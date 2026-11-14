package com.example.data

import com.example.R
import com.example.model.CitySpot
import com.example.model.FaqItem
import com.example.model.ServiceCategory
import com.example.model.ServiceItem
import com.example.model.ServicePackageTier
import com.example.model.SpecialOffer

class CatalogRepository {

    val cities: List<CitySpot> = listOf(
        CitySpot("Mumbai", "Bandra, Worli & Colaba", 48),
        CitySpot("New Delhi", "Lutyens & Vasant Vihar", 39),
        CitySpot("Bengaluru", "Indiranagar & Sadashivanagar", 34),
        CitySpot("Goa", "Assagao & North Coast", 22),
        CitySpot("Dubai", "DIFC & Palm Jumeirah", 31),
        CitySpot("London", "Mayfair & Kensington", 27)
    )

    val specialOffers: List<SpecialOffer> = listOf(
        SpecialOffer(
            id = "offer_winter_reserve",
            badge = "MEMBER PRIVILEGE",
            title = "Complimentary Residence Upgrade",
            subtitle = "15% privilege on Signature Spa & Thermal Suites this season.",
            code = "AUREVIA15",
            discountPercent = 15
        ),
        SpecialOffer(
            id = "offer_travel_concierge",
            badge = "CONCIERGE SUITE",
            title = "Priority Event & Travel Pairing",
            subtitle = "10% privilege on vetted cultural & gallery companionship bookings.",
            code = "PRIVILEGE10",
            discountPercent = 10
        )
    )

    val faqs: List<FaqItem> = listOf(
        FaqItem(
            id = "faq_1",
            category = "Trust & Safety",
            question = "How does AUREVIA verify specialists and companions?",
            answer = "Every specialist and social companion undergoes multi-tier government ID verification, background screening, credential checks, and mandatory code-of-conduct training prior to onboarding."
        ),
        FaqItem(
            id = "faq_2",
            category = "Boundaries",
            question = "What are the boundaries for Social & Event Companionship?",
            answer = "All AUREVIA companionship services are strictly non-sexual, lawful, adult-only (18+) social engagements in public venues, hospitality lounges, cultural events, or accredited travel itineraries. Zero tolerance applies to any boundary violation."
        ),
        FaqItem(
            id = "faq_3",
            category = "Payments & Refunds",
            question = "Does AUREVIA store my debit or credit card details?",
            answer = "Never. Payment instruments are tokenized with masked display references only (such as last 4 digits or UPI handle). Full digital receipts are generated upon gateway confirmation."
        ),
        FaqItem(
            id = "faq_4",
            category = "Cancellations",
            question = "What is the cancellation and refund policy?",
            answer = "Bookings cancelled at least 24 hours before the scheduled slot qualify for a 100% refund to the original payment method. Cancellations within 24 hours receive a 50% credit or refund after concierge review."
        ),
        FaqItem(
            id = "faq_5",
            category = "Privacy",
            question = "Is my personal identity visible on public reviews?",
            answer = "Never. Only verified completed bookings may post a public review, and only a masked display name (first name and last initial) is shown."
        )
    )

    val services: List<ServiceItem> = listOf(
        ServiceItem(
            id = "spa_ayurvedic_ritual",
            title = "Royal Ayurvedic Thermal Ritual",
            category = ServiceCategory.SPA_WELLNESS,
            providerId = "prov_soraia_spa",
            providerName = "Soraia Wellness Collective",
            providerTitle = "Accredited Ayurvedic & Spa Sanctuary",
            shortSummary = "Warm herbal abhyanga, marble thermal compress, and restorative scalp therapy.",
            description = "An unhurried, deeply restorative wellness session delivered in our partner sanctuaries or private residence suites. Formulated with organic cold-pressed botanical oils and guided breathwork.",
            imageRes = R.drawable.img_spa_wellness,
            startingPriceInr = 7500,
            durationRange = "60 – 120 min",
            rating = 4.9,
            reviewCount = 142,
            cities = listOf("Mumbai", "New Delhi", "Bengaluru", "Goa"),
            availableDates = listOf("2026-11-15", "2026-11-16", "2026-11-18", "2026-11-20", "2026-11-22"),
            availableTimeSlots = listOf("10:00 AM", "01:30 PM", "04:00 PM", "07:00 PM"),
            includedItems = listOf(
                "Certified senior wellness therapist",
                "Organic cold-pressed botanical oils & heated linens",
                "Pre-session wellness & pressure consultation",
                "Post-ritual saffron-cardamom herbal infusion"
            ),
            excludedAndBoundaries = listOf(
                "Strictly therapeutic, non-medical, and non-sexual wellness care",
                "Out-of-zone travel surcharge outside city limits",
                "Medical diagnostics or clinical physiotherapy prescriptions"
            ),
            cancellationPolicy = "Free cancellation or rescheduling up to 24 hours prior to arrival. 50% fee applies within 24 hours.",
            safetyInformation = "Therapists carry verified Aurevia digital ID, sanitized single-use kits, and live concierge check-in support.",
            isFeatured = true,
            isAdultVerifiedOnly = false,
            packages = listOf(
                ServicePackageTier(
                    id = "spa_essential",
                    name = "Essential",
                    durationMinutes = 60,
                    priceInr = 7500,
                    summary = "Focused restorative bodywork & breathwork.",
                    highlights = listOf("60 min thermal bodywork", "Botanical oil blend", "Herbal tea service")
                ),
                ServicePackageTier(
                    id = "spa_signature",
                    name = "Signature",
                    durationMinutes = 90,
                    priceInr = 11500,
                    summary = "Full-body Abhyanga plus warm marble compress.",
                    highlights = listOf("90 min full ritual", "Warm stone & scalp therapy", "Aromatherapy kit")
                ),
                ServicePackageTier(
                    id = "spa_bespoke",
                    name = "Bespoke Suite",
                    durationMinutes = 120,
                    priceInr = 16500,
                    summary = "Two-hour sanctuary immersion with facial lymphatic sculpt.",
                    highlights = listOf("120 min private suite", "Lymphatic facial sculpt", "Priority concierge host")
                )
            )
        ),
        ServiceItem(
            id = "fit_private_reformer",
            title = "Private Strength & Mobility Atelier",
            category = ServiceCategory.FITNESS_TRAINING,
            providerId = "prov_kinetix_studio",
            providerName = "Aarohan Movement Studio",
            providerTitle = "NSCA-Certified Performance Specialists",
            shortSummary = "One-on-one posture correction, athletic conditioning, and recovery stretching.",
            description = "Tailored strength, mobility, and breath-led athletic conditioning designed around your travel schedule and recovery goals—hosted in private studio suites or residence gyms.",
            imageRes = R.drawable.img_hero_sanctuary,
            startingPriceInr = 4500,
            durationRange = "60 – 90 min",
            rating = 4.8,
            reviewCount = 96,
            cities = listOf("Mumbai", "New Delhi", "Bengaluru", "Dubai", "London"),
            availableDates = listOf("2026-11-15", "2026-11-16", "2026-11-17", "2026-11-19", "2026-11-21"),
            availableTimeSlots = listOf("07:00 AM", "09:30 AM", "05:00 PM", "07:30 PM"),
            includedItems = listOf(
                "Certified personal performance coach",
                "Movement & joint mobility screen",
                "Portable resistance & recovery tools",
                "Post-session hydration & recovery summary"
            ),
            excludedAndBoundaries = listOf(
                "Commercial gym entry passes for external guests",
                "Dietary supplements or prescription meal plans",
                "High-risk rehab without physician clearance"
            ),
            cancellationPolicy = "Complimentary reschedule up to 12 hours before session start.",
            safetyInformation = "All coaches hold CPR/AED certification and verified background clearance.",
            isFeatured = true,
            isAdultVerifiedOnly = false,
            packages = listOf(
                ServicePackageTier(
                    id = "fit_essential",
                    name = "Essential",
                    durationMinutes = 60,
                    priceInr = 4500,
                    summary = "60-minute targeted strength & core session.",
                    highlights = listOf("1-on-1 coaching", "Mobility warm-up", "Session recap")
                ),
                ServicePackageTier(
                    id = "fit_signature",
                    name = "Signature",
                    durationMinutes = 75,
                    priceInr = 6800,
                    summary = "Conditioning plus assisted PNF stretch recovery.",
                    highlights = listOf("75 min training", "Assisted stretch", "Travel workout plan")
                ),
                ServicePackageTier(
                    id = "fit_bespoke",
                    name = "Bespoke",
                    durationMinutes = 90,
                    priceInr = 9500,
                    summary = "Full athletic assessment, training & percussive recovery.",
                    highlights = listOf("90 min studio suite", "Biometric baseline", "Custom 7-day protocol")
                )
            )
        ),
        ServiceItem(
            id = "life_executive_concierge",
            title = "Executive Lifestyle & Wardrobe Concierge",
            category = ServiceCategory.LIFESTYLE_ASSISTANCE,
            providerId = "prov_maison_assist",
            providerName = "Maison Valet Desk",
            providerTitle = "Vetted Personal Lifestyle Specialists",
            shortSummary = "Discreet wardrobe curation, bespoke gifting, and residence preparation.",
            description = "Dedicated lifestyle assistance for busy executives and visiting guests—covering private shopping coordination, luggage packing/unpacking, luxury gifting, and residence readiness.",
            imageRes = R.drawable.img_hospitality_dining,
            startingPriceInr = 6000,
            durationRange = "120 – 240 min",
            rating = 4.9,
            reviewCount = 68,
            cities = listOf("Mumbai", "New Delhi", "Dubai", "London"),
            availableDates = listOf("2026-11-15", "2026-11-17", "2026-11-18", "2026-11-20"),
            availableTimeSlots = listOf("10:30 AM", "02:00 PM", "05:30 PM"),
            includedItems = listOf(
                "NDA-bound vetted lifestyle specialist",
                "Pre-service brief & itinerary alignment",
                "Real-time expense receipts for approved purchases"
            ),
            excludedAndBoundaries = listOf(
                "Cost of third-party retail items or courier charges",
                "Financial/legal power-of-attorney representation",
                "Transporting restricted or undeclared items"
            ),
            cancellationPolicy = "100% refund up to 24 hours prior; 50% thereafter.",
            safetyInformation = "All lifestyle specialists sign strict confidentiality & anti-fraud charters.",
            isFeatured = false,
            isAdultVerifiedOnly = false,
            packages = listOf(
                ServicePackageTier(
                    id = "life_essential",
                    name = "Essential",
                    durationMinutes = 120,
                    priceInr = 6000,
                    summary = "2 hours of dedicated errand & gifting curation.",
                    highlights = listOf("2-hour specialist block", "Digital receipt log", "Concierge wrap-up")
                ),
                ServicePackageTier(
                    id = "life_signature",
                    name = "Signature",
                    durationMinutes = 180,
                    priceInr = 8800,
                    summary = "3 hours including wardrobe & residence prep.",
                    highlights = listOf("3-hour coverage", "Wardrobe steaming & prep", "Vendor coordination")
                ),
                ServicePackageTier(
                    id = "life_bespoke",
                    name = "Bespoke Half-Day",
                    durationMinutes = 240,
                    priceInr = 12500,
                    summary = "4-hour private executive desk support.",
                    highlights = listOf("4-hour dedicated concierge", "Priority courier dispatch", "NDA protection")
                )
            )
        ),
        ServiceItem(
            id = "hosp_private_sommelier",
            title = "Private Terrace Dining & Sommelier Host",
            category = ServiceCategory.HOSPITALITY,
            providerId = "prov_atelier_table",
            providerName = "L'Atelier Table & Cellar",
            providerTitle = "Curated Culinary & Hospitality Team",
            shortSummary = "Multi-course private dining table curation with non-alcoholic or fine pairing host.",
            description = "Transform your private terrace or dining room into an intimate Michelin-inspired hospitality setting with curated table styling, chef-prepared courses, and attentive white-glove hosting.",
            imageRes = R.drawable.img_hospitality_dining,
            startingPriceInr = 14000,
            durationRange = "150 – 240 min",
            rating = 5.0,
            reviewCount = 81,
            cities = listOf("Mumbai", "New Delhi", "Bengaluru", "Goa", "Dubai"),
            availableDates = listOf("2026-11-16", "2026-11-18", "2026-11-20", "2026-11-21", "2026-11-23"),
            availableTimeSlots = listOf("01:00 PM", "07:00 PM", "08:30 PM"),
            includedItems = listOf(
                "Private culinary host & sommelier consultation",
                "Linen, brass candleware & seasonal floral accents",
                "4-course artisanal tasting menu for two",
                "Full post-dining kitchen & table reset"
            ),
            excludedAndBoundaries = listOf(
                "Alcoholic beverages where local licensing restricts supply",
                "Additional guest covers beyond selected tier",
                "Venue rental fees at external residences"
            ),
            cancellationPolicy = "Free cancellation up to 48 hours before service due to fresh ingredient sourcing.",
            safetyInformation = "FSSAI-certified kitchen partners and hygiene-verified hospitality staff.",
            isFeatured = true,
            isAdultVerifiedOnly = false,
            packages = listOf(
                ServicePackageTier(
                    id = "hosp_essential",
                    name = "Essential Tasting",
                    durationMinutes = 150,
                    priceInr = 14000,
                    summary = "4-course artisanal dinner for 2 guests.",
                    highlights = listOf("4-course menu for 2", "Table styling", "Clean-up included")
                ),
                ServicePackageTier(
                    id = "hosp_signature",
                    name = "Signature Pairing",
                    durationMinutes = 180,
                    priceInr = 21000,
                    summary = "5-course tasting with botanical or cellar pairing.",
                    highlights = listOf("5-course menu for 2", "Dedicated sommelier host", "Floral centerpiece")
                ),
                ServicePackageTier(
                    id = "hosp_bespoke",
                    name = "Bespoke Soiree",
                    durationMinutes = 240,
                    priceInr = 32000,
                    summary = "Private soirée hosting for up to 6 guests.",
                    highlights = listOf("Up to 6 guests", "Private chef + server", "Custom printed menus")
                )
            )
        ),
        ServiceItem(
            id = "comp_social_dialogue",
            title = "Curated Cultural & Dining Companionship",
            category = ServiceCategory.SOCIAL_COMPANIONSHIP,
            providerId = "prov_aurevia_guild",
            providerName = "Aurevia Cultural Guild",
            providerTitle = "Verified 18+ Social & Etiquette Companions",
            shortSummary = "Refined, strictly non-sexual social conversation for fine dining, art walks, or tea.",
            description = "Designed for solo travelers, expats, and professionals seeking articulate, culturally fluent company for fine dining, museum visits, or café conversation. Strictly non-sexual with verified public-venue protocols.",
            imageRes = R.drawable.img_companionship_events,
            startingPriceInr = 8500,
            durationRange = "90 – 180 min",
            rating = 4.9,
            reviewCount = 54,
            cities = listOf("Mumbai", "New Delhi", "Bengaluru", "Dubai", "London"),
            availableDates = listOf("2026-11-15", "2026-11-16", "2026-11-19", "2026-11-22"),
            availableTimeSlots = listOf("11:30 AM", "04:00 PM", "07:30 PM"),
            includedItems = listOf(
                "ID-verified, background-checked adult (18+) companion",
                "Fluency in arts, business etiquette & local culture",
                "Active concierge safety check-in at start and end"
            ),
            excludedAndBoundaries = listOf(
                "STRICTLY NON-SEXUAL: Zero romantic, intimate, or physical solicitation permitted",
                "Public hospitality, cultural, or accredited event venues only",
                "Dining/ticket expenses are billed separately at venue",
                "Both parties must be 18+ and accept the Aurevia Mutual Respect Charter"
            ),
            cancellationPolicy = "100% refund up to 24 hours prior. Immediate zero-refund termination if safety boundaries are breached.",
            safetyInformation = "Mandatory 18+ verification, public-venue GPS check-in option, and 24/7 Trust & Safety escalation line.",
            isFeatured = true,
            isAdultVerifiedOnly = true,
            packages = listOf(
                ServicePackageTier(
                    id = "comp_essential",
                    name = "Afternoon Salon",
                    durationMinutes = 90,
                    priceInr = 8500,
                    summary = "90-minute gallery walk or high-tea conversation.",
                    highlights = listOf("90 min public venue", "Verified cultural host", "Safety check-in")
                ),
                ServicePackageTier(
                    id = "comp_signature",
                    name = "Evening Dining",
                    durationMinutes = 120,
                    priceInr = 12000,
                    summary = "2-hour fine dining or theatre evening company.",
                    highlights = listOf("120 min evening slot", "Pre-event briefing", "Concierge coordination")
                ),
                ServicePackageTier(
                    id = "comp_bespoke",
                    name = "Bespoke Cultural Day",
                    durationMinutes = 180,
                    priceInr = 17500,
                    summary = "3-hour curated museum, architecture & dining tour.",
                    highlights = listOf("180 min itinerary", "Multilingual pairing", "Priority support line")
                )
            )
        ),
        ServiceItem(
            id = "event_gala_travel",
            title = "Gala, Summit & Travel Companion Host",
            category = ServiceCategory.EVENT_TRAVEL,
            providerId = "prov_diplomat_circle",
            providerName = "Diplomat Circle Concierge",
            providerTitle = "Verified Event & Destination Hosts (18+)",
            shortSummary = "Polished, strictly non-sexual companionship for corporate galas, weddings, and city tours.",
            description = "Arrive confidently at formal galas, art biennales, charity dinners, or curated destination walks with a vetted, well-traveled Aurevia companion who upholds impeccable etiquette and strict professional boundaries.",
            imageRes = R.drawable.img_companionship_events,
            startingPriceInr = 12500,
            durationRange = "180 – 360 min",
            rating = 4.9,
            reviewCount = 43,
            cities = listOf("Mumbai", "New Delhi", "Goa", "Dubai", "London"),
            availableDates = listOf("2026-11-17", "2026-11-19", "2026-11-21", "2026-11-24"),
            availableTimeSlots = listOf("10:00 AM", "06:00 PM", "07:30 PM"),
            includedItems = listOf(
                "Formal black-tie / business attire readiness",
                "Verified adult (18+) event & itinerary companion",
                "Dedicated Aurevia safety desk monitoring throughout event"
            ),
            excludedAndBoundaries = listOf(
                "STRICTLY NON-SEXUAL: Professional social & event presence only",
                "Private overnight hotel room stays are strictly excluded",
                "Client covers official event entry pass & scheduled transit"
            ),
            cancellationPolicy = "Free cancellation up to 48 hours prior to scheduled event.",
            safetyInformation = "Verified 18+ identity, mutual consent charter required, and instant Block/Report protection.",
            isFeatured = false,
            isAdultVerifiedOnly = true,
            packages = listOf(
                ServicePackageTier(
                    id = "ev_essential",
                    name = "Reception Host",
                    durationMinutes = 180,
                    priceInr = 12500,
                    summary = "3-hour formal gala or exhibition accompaniment.",
                    highlights = listOf("180 min event presence", "Formal dress code", "Safety desk check-in")
                ),
                ServicePackageTier(
                    id = "ev_signature",
                    name = "City & Travel Day",
                    durationMinutes = 240,
                    priceInr = 18000,
                    summary = "4-hour curated heritage, dining & city orientation.",
                    highlights = listOf("240 min daytime guide", "Custom route planning", "Concierge coordination")
                ),
                ServicePackageTier(
                    id = "ev_bespoke",
                    name = "Bespoke Gala Suite",
                    durationMinutes = 360,
                    priceInr = 26000,
                    summary = "6-hour executive summit or destination gala coverage.",
                    highlights = listOf("360 min full coverage", "Pre-event briefing call", "Priority escalation line")
                )
            )
        )
    )

    fun getServiceById(serviceId: String): ServiceItem? =
        services.firstOrNull { it.id == serviceId }
}
