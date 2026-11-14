const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_aurevia_1";
const BOB_UID = "bob_aurevia_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowTs() {
  return new Date(Date.now() - 2000);
}

function validProfile(uid) {
  return {
    userId: uid,
    fullName: "Aarav Mehta",
    email: "aarav@example.com",
    mobileNumber: "+91 98200 12345",
    preferredCity: "Mumbai",
    emergencyContact: "+91 98200 99999",
    isAdultConfirmed: true,
    blockedProviderIds: [],
    notifyEmail: true,
    notifySms: true,
    notifyWhatsApp: false,
    notifyPush: true,
    sessionEpoch: 1,
    createdAt: nowTs(),
    updatedAt: nowTs(),
  };
}

function validBooking(uid, status = "PENDING_PAYMENT", paymentStatus = "PENDING") {
  return {
    userId: uid,
    bookingCode: "AUR-20261113-1001",
    serviceId: "spa_ayurvedic_ritual",
    serviceTitle: "Royal Ayurvedic Thermal Ritual",
    category: "Spa & Wellness",
    providerName: "Soraia Wellness Collective",
    packageName: "Signature Suite (90 min)",
    city: "Mumbai",
    addressSummary: "Taj Lands End Residences, Bandra West",
    scheduledDate: "2026-11-20",
    scheduledTimeSlot: "16:00",
    durationMinutes: 90,
    subtotalInr: 12000,
    taxInr: 2160,
    totalInr: 14160,
    guestNotes: "Prefer quiet ambiance",
    consentAccepted: true,
    status,
    paymentStatus,
    paymentMethod: "UPI",
    transactionRef: "",
    cancellationReason: "",
    createdAt: nowTs(),
    updatedAt: nowTs(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user profiles or bookings", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bookings").get());
  await assertFails(unauthDb.collection("service_reviews").get());
});

test("Owner can create and read own profile; cannot read or list another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
});

test("Shadow update with ghost field on user profile is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badPayload = { ...validProfile(ALICE_UID), isAdmin: true };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badPayload));
});

test("Booking requires consentAccepted == true and blocks cross-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const noConsent = { ...validBooking(ALICE_UID), consentAccepted: false };
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("b_no_consent").set(noConsent)
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("b_1").set(validBooking(ALICE_UID))
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("bookings").doc("b_1").get()
  );
});

test("Booking state transitions enforce valid lifecycle and terminal lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bookingRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("b_flow");

  await assertSucceeds(bookingRef.set(validBooking(ALICE_UID)));

  // Confirm payment
  await assertSucceeds(bookingRef.update({
    status: "CONFIRMED",
    paymentStatus: "PAID",
    transactionRef: "UPI-REF-9988",
    updatedAt: nowTs(),
  }));

  // Complete service
  await assertSucceeds(bookingRef.update({
    status: "COMPLETED",
    updatedAt: nowTs(),
  }));

  // Cannot cancel after COMPLETED
  await assertFails(bookingRef.update({
    status: "CANCELLED",
    paymentStatus: "REFUND_REQUESTED",
    cancellationReason: "Changed mind",
    updatedAt: nowTs(),
  }));
});

test("Public service review requires COMPLETED booking owned by caller and rejects PII ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bookingRef = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("b_rev");
  await assertSucceeds(bookingRef.set(validBooking(ALICE_UID, "CONFIRMED", "PAID")));

  // Fails while CONFIRMED (not COMPLETED)
  await assertFails(aliceDb.collection("service_reviews").doc("rev_1").set({
    userId: ALICE_UID,
    reviewerDisplayName: "Aarav M.",
    serviceId: "spa_ayurvedic_ritual",
    bookingId: "b_rev",
    rating: 5,
    comment: "Remarkable ritual and serene environment.",
    isVerifiedBooking: true,
    createdAt: nowTs(),
  }));

  // Mark COMPLETED
  await assertSucceeds(bookingRef.update({ status: "COMPLETED", updatedAt: nowTs() }));

  // Succeeds once COMPLETED
  await assertSucceeds(aliceDb.collection("service_reviews").doc("rev_1").set({
    userId: ALICE_UID,
    reviewerDisplayName: "Aarav M.",
    serviceId: "spa_ayurvedic_ritual",
    bookingId: "b_rev",
    rating: 5,
    comment: "Remarkable ritual and serene environment.",
    isVerifiedBooking: true,
    createdAt: nowTs(),
  }));

  // Rejects PII ghost field
  await assertFails(aliceDb.collection("service_reviews").doc("rev_2").set({
    userId: ALICE_UID,
    reviewerDisplayName: "Aarav M.",
    email: "aarav@example.com",
    serviceId: "spa_ayurvedic_ritual",
    bookingId: "b_rev",
    rating: 5,
    comment: "Testing ghost field rejection.",
    isVerifiedBooking: true,
    createdAt: nowTs(),
  }));
});

test("Support ticket and message creation enforce parent ticket existence", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ticketRef = aliceDb.collection("users").doc(ALICE_UID).collection("tickets").doc("t_1");

  // Message without parent ticket fails
  await assertFails(ticketRef.collection("messages").doc("m_1").set({
    userId: ALICE_UID,
    senderRole: "CUSTOMER",
    body: "Hello concierge",
    createdAt: nowTs(),
  }));

  await assertSucceeds(ticketRef.set({
    userId: ALICE_UID,
    bookingId: "",
    category: "GENERAL",
    subject: "Concierge inquiry",
    description: "Can I request aromatherapy oil preferences?",
    status: "OPEN",
    createdAt: nowTs(),
    updatedAt: nowTs(),
  }));

  await assertSucceeds(ticketRef.collection("messages").doc("m_1").set({
    userId: ALICE_UID,
    senderRole: "CUSTOMER",
    body: "Hello concierge",
    createdAt: nowTs(),
  }));

  // Atomic batch creation of ticket + first message + notification
  const batch = aliceDb.batch();
  const tBatchRef = aliceDb.collection("users").doc(ALICE_UID).collection("tickets").doc("t_batch");
  const mBatchRef = tBatchRef.collection("messages").doc("m_batch");
  const nBatchRef = aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("n_batch");
  batch.set(tBatchRef, {
    userId: ALICE_UID,
    bookingId: "",
    category: "GENERAL",
    subject: "Batch concierge inquiry",
    description: "Testing atomic ticket + message + notification batch.",
    status: "OPEN",
    createdAt: nowTs(),
    updatedAt: nowTs(),
  });
  batch.set(mBatchRef, {
    userId: ALICE_UID,
    senderRole: "CUSTOMER",
    body: "Testing atomic ticket + message + notification batch.",
    createdAt: nowTs(),
  });
  batch.set(nBatchRef, {
    userId: ALICE_UID,
    title: "Support Ticket Opened",
    message: "Concierge Desk logged: Batch concierge inquiry",
    type: "SUPPORT",
    isRead: false,
    relatedId: "t_batch",
    createdAt: nowTs(),
  });
  await assertSucceeds(batch.commit());
});
