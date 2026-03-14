import Foundation

nonisolated enum MockData {

    static let allConversations: [Conversation] = [
        faultFinderConversation,
        learnConversation,
        researchConversation
    ]

    // MARK: - Fault Finder — RCD Tripping

    static let faultFinderConversation = Conversation(
        title: "RCD keeps tripping",
        messages: [
            ChatMessage(
                role: .user,
                blocks: [
                    ContentBlock(type: .text, content: "The RCD on my switchboard keeps tripping every time I turn on the kitchen lights. It's a Clipsal 4RC440/30 on a domestic board. What should I check?")
                ],
                mode: .faultFinder
            ),
            ChatMessage(
                role: .assistant,
                blocks: [
                    ContentBlock(type: .text, content: "An RCD tripping when a specific circuit is loaded usually indicates an earth leakage fault on that circuit. Since it only trips with the kitchen lights, the fault is likely isolated to the lighting sub-circuit. Here is a systematic approach to diagnose it."),
                    ContentBlock(
                        type: .stepList,
                        title: "RCD Trip Diagnosis Procedure",
                        steps: [
                            "Isolate the kitchen lighting circuit at the switchboard by switching off its MCB.",
                            "Reset the RCD. If it holds, the fault is confirmed on that circuit.",
                            "Disconnect all luminaires on the kitchen circuit at their terminal blocks.",
                            "Perform an insulation resistance test (IR test) between active-earth and neutral-earth on the circuit cabling. You need a minimum of 1 MΩ at 500V DC per AS/NZS 3017.",
                            "If cabling passes, reconnect luminaires one at a time, testing the RCD after each reconnection.",
                            "The luminaire that causes the trip is your fault source — check its wiring, terminations, and the fitting for moisture ingress.",
                            "Once the faulty fitting is identified, inspect for damaged insulation, pinched cables, or water in the junction box.",
                            "Repair or replace the faulty fitting and re-test the full circuit IR before restoring supply."
                        ]
                    ),
                    ContentBlock(type: .warning, content: "Always isolate and lock out the circuit before performing insulation resistance testing. IR testers apply 500V DC which is lethal. Confirm zero volts with a proving unit before touching any conductors."),
                    ContentBlock(
                        type: .partsList,
                        title: "Common Replacement Parts",
                        items: [
                            PartsItem(name: "Clipsal 4RC440/30 RCD", spec: "4-pole 40A 30mA Type A", qty: 1),
                            PartsItem(name: "LED Downlight", spec: "IC-4 rated, IP44, AS/NZS 60598.2.2", qty: 1),
                            PartsItem(name: "Terminal Connector", spec: "3-pole lever connector 32A rated", qty: 6),
                            PartsItem(name: "TPS Cable", spec: "2.5mm² Twin & Earth 450/750V", qty: 10)
                        ]
                    ),
                    ContentBlock(
                        type: .regulation,
                        content: "Residual current devices protecting final sub-circuits must have a rated residual operating current not exceeding 30 mA.",
                        code: "AS/NZS 3000:2018",
                        clause: "Clause 2.6.3.2",
                        summary: "All final sub-circuits in domestic installations require 30mA RCD protection. The RCD must be tested at installation and at regular intervals using the built-in test button."
                    )
                ],
                mode: .faultFinder
            ),
            ChatMessage(
                role: .user,
                blocks: [
                    ContentBlock(type: .text, content: "I did the IR test and got 0.3 MΩ between active and earth on the circuit. That's a fail right?")
                ],
                mode: .faultFinder
            ),
            ChatMessage(
                role: .assistant,
                blocks: [
                    ContentBlock(type: .text, content: "Correct — 0.3 MΩ is well below the minimum 1 MΩ required by AS/NZS 3017 Table 8.1 for a 230V circuit. This confirms an insulation breakdown between active and earth somewhere on the circuit."),
                    ContentBlock(
                        type: .stepList,
                        title: "Narrowing Down the Fault",
                        steps: [
                            "Disconnect the circuit at every junction point, splitting it into individual cable runs.",
                            "IR test each cable run individually between active-earth and neutral-earth.",
                            "The run that reads below 1 MΩ contains the damaged section.",
                            "Inspect that cable run for staple damage, rodent activity, or moisture ingress at penetrations.",
                            "Replace the damaged cable section and re-test the full circuit."
                        ]
                    ),
                    ContentBlock(type: .warning, content: "Do not energise a circuit with insulation resistance below 1 MΩ. This presents a shock risk and will continue to trip the RCD. The fault must be rectified before restoring supply.")
                ],
                mode: .faultFinder
            )
        ],
        mode: .faultFinder
    )

    // MARK: - Learn — Cable Sizing

    static let learnConversation = Conversation(
        title: "Cable sizing for power circuits",
        messages: [
            ChatMessage(
                role: .user,
                blocks: [
                    ContentBlock(type: .text, content: "Can you explain how to size cables for a standard domestic power circuit? I'm studying for my capstone and want to understand the process properly.")
                ],
                mode: .learn
            ),
            ChatMessage(
                role: .assistant,
                blocks: [
                    ContentBlock(type: .heading, content: "Cable Sizing for Domestic Power Circuits", level: 1),
                    ContentBlock(type: .text, content: "Cable sizing involves matching the conductor cross-sectional area to the expected load current while accounting for installation conditions that reduce the cable's current-carrying capacity. The process defined in AS/NZS 3008.1 follows three main steps: determine the design current, apply derating factors, then verify voltage drop."),
                    ContentBlock(type: .heading, content: "Step 1 — Design Current", level: 2),
                    ContentBlock(type: .text, content: "For a standard domestic GPO circuit protected by a 20A MCB, the design current is the MCB rating: 20A. The cable must be rated to carry at least this current continuously under the installed conditions."),
                    ContentBlock(type: .heading, content: "Step 2 — Derating Factors", level: 2),
                    ContentBlock(type: .text, content: "The base current rating from AS/NZS 3008.1 Table 3 assumes a single circuit at 40°C ambient in free air. Real installations require derating for grouping, ambient temperature, thermal insulation contact, and installation method."),
                    ContentBlock(
                        type: .code,
                        content: "Cable Sizing Reference — AS/NZS 3008.1 Table 3 (Enclosed in Conduit/Trunking)\n\nConductor     | 2-core flat | 2C+E flat | Circular\n1.0 mm²       |    12A      |    12A    |   11A\n1.5 mm²       |    15A      |    15A    |   14A\n2.5 mm²       |    20A      |    20A    |   18A\n4.0 mm²       |    27A      |    25A    |   24A\n6.0 mm²       |    34A      |    32A    |   31A\n10.0 mm²      |    46A      |    43A    |   42A",
                        language: "text"
                    ),
                    ContentBlock(
                        type: .callout,
                        content: "For a 20A GPO circuit run in TPS through ceiling insulation, use 2.5mm² as the starting point. If the cable passes through insulation for more than 200mm, apply a derating factor of 0.5 (Table 25 of AS/NZS 3008.1), which means the effective rating drops to 10A — below the 20A MCB. In that case, upsize to 4.0mm².",
                        style: "tip"
                    ),
                    ContentBlock(
                        type: .table,
                        headers: ["Cable Size", "Base Rating (Enclosed)", "Derated (Insulation)", "Typical Use"],
                        rows: [
                            ["1.0 mm²", "12A", "6A", "Lighting circuits (10A MCB)"],
                            ["1.5 mm²", "15A", "7.5A", "Lighting circuits (16A MCB)"],
                            ["2.5 mm²", "20A", "10A", "Standard GPO circuits (20A MCB)"],
                            ["4.0 mm²", "27A", "13.5A", "High-load GPO or cooktop circuits"],
                            ["6.0 mm²", "34A", "17A", "Ovens, hot water, large A/C"],
                            ["10.0 mm²", "46A", "23A", "Sub-mains, EV chargers"]
                        ]
                    )
                ],
                mode: .learn
            ),
            ChatMessage(
                role: .user,
                blocks: [
                    ContentBlock(type: .text, content: "What about voltage drop? How do I check that?")
                ],
                mode: .learn
            ),
            ChatMessage(
                role: .assistant,
                blocks: [
                    ContentBlock(type: .heading, content: "Voltage Drop Verification", level: 2),
                    ContentBlock(type: .text, content: "AS/NZS 3000:2018 Clause 3.6.2 limits the total voltage drop from the point of supply to the furthest point of utilisation to 5% of the nominal voltage. For a 230V supply, that is a maximum of 11.5V total. This includes the drop in the consumer mains and the sub-circuit."),
                    ContentBlock(
                        type: .code,
                        content: "Voltage Drop Formula:\n\nVd = (I × L × Vc) / 1000\n\nWhere:\n  Vd = Voltage drop (volts)\n  I  = Design current (amps)\n  L  = Route length of cable (metres, one way)\n  Vc = Voltage drop per amp per metre (mV/A/m)\n       from AS/NZS 3008.1 Table 40\n\nExample: 20A circuit, 25m run, 2.5mm² TPS\n  Vc for 2.5mm² = 18.1 mV/A/m\n  Vd = (20 × 25 × 18.1) / 1000 = 9.05V\n  9.05V / 230V = 3.9% — PASS (under 5%)",
                        language: "text"
                    ),
                    ContentBlock(
                        type: .callout,
                        content: "Always calculate voltage drop for long cable runs, especially in large houses or sheds. If the voltage drop exceeds 5%, either increase the cable size or reduce the circuit length by relocating the switchboard or adding a sub-board closer to the load.",
                        style: "important"
                    )
                ],
                mode: .learn
            )
        ],
        mode: .learn
    )

    // MARK: - Research — EV Charger Installation

    static let researchConversation = Conversation(
        title: "EV charger installation requirements",
        messages: [
            ChatMessage(
                role: .user,
                blocks: [
                    ContentBlock(type: .text, content: "What are the requirements for installing a Level 2 EV charger in a residential property in Australia? I need to know the regulatory requirements and what specs to look at.")
                ],
                mode: .research
            ),
            ChatMessage(
                role: .assistant,
                blocks: [
                    ContentBlock(type: .heading, content: "Residential EV Charger Installation — Australia", level: 1),
                    ContentBlock(type: .text, content: "Level 2 EV chargers operate on single-phase or three-phase supply at 240V/415V AC and require a dedicated circuit from the switchboard. Installation must comply with AS/NZS 3000:2018 (Wiring Rules), AS/NZS 3001 for transportable structures if applicable, and any state-specific regulations. The charger itself must comply with AS/NZS 61851 (EV conductive charging systems)."),
                    ContentBlock(
                        type: .regulation,
                        content: "Electrical equipment for electric vehicle charging must be installed in accordance with the requirements of this Standard and the equipment manufacturer's instructions.",
                        code: "AS/NZS 3000:2018",
                        clause: "Clause 4.14",
                        summary: "Section 4.14 covers EV charging installations specifically, including requirements for dedicated circuits, RCD protection, cable sizing, and installation of Mode 2 and Mode 3 charging equipment."
                    ),
                    ContentBlock(
                        type: .link,
                        content: "AS/NZS 61851.1:2014 — Electric vehicle conductive charging system: General requirements",
                        url: "https://www.standards.org.au/standards-catalogue/sa-snz/electrotechnology/el-055/as-slash-nzs--61851-dot-1-colon-2014"
                    ),
                    ContentBlock(
                        type: .table,
                        headers: ["Specification", "Single-Phase", "Three-Phase"],
                        rows: [
                            ["Supply Voltage", "230V AC", "415V AC"],
                            ["Typical Current", "32A", "32A per phase"],
                            ["Power Output", "7.4 kW", "22 kW"],
                            ["Minimum Cable Size", "6.0 mm² TPS", "6.0 mm² 4-core+E"],
                            ["MCB Rating", "40A Type C", "40A 3-pole Type C"],
                            ["RCD Requirement", "30mA Type A or Type B", "30mA Type B (recommended)"],
                            ["Dedicated Circuit", "Required", "Required"],
                            ["Metering", "Optional sub-meter", "Recommended sub-meter"]
                        ]
                    )
                ],
                mode: .research
            ),
            ChatMessage(
                role: .user,
                blocks: [
                    ContentBlock(type: .text, content: "Does the customer need a switchboard upgrade? Their board is already pretty full and they have a 63A main switch.")
                ],
                mode: .research
            ),
            ChatMessage(
                role: .assistant,
                blocks: [
                    ContentBlock(type: .text, content: "A 63A single-phase main switch limits the total available capacity. A 7.4 kW charger draws 32A continuously, which is over 50% of the main switch rating. You need to perform a maximum demand calculation per AS/NZS 3000:2018 Appendix C to determine if the existing supply can support the additional load."),
                    ContentBlock(
                        type: .stepList,
                        title: "Maximum Demand Assessment",
                        steps: [
                            "List all existing fixed loads: hot water, oven, cooktop, air conditioning, pool equipment.",
                            "Apply diversity factors from AS/NZS 3000 Appendix C Table C1 to calculate the after-diversity maximum demand (ADMD).",
                            "Add the EV charger load (32A for single-phase Level 2) with no diversity — it is a continuous load.",
                            "Compare the total ADMD against the 63A main switch and service fuse rating.",
                            "If total exceeds 63A, options include: upgrading the main switch and service fuse, installing a load management device, or selecting a lower-rated charger (e.g., 15A / 3.6 kW)."
                        ]
                    ),
                    ContentBlock(
                        type: .regulation,
                        content: "The maximum demand of an electrical installation shall be assessed in accordance with Appendix C to ensure the consumer mains and service line are adequate.",
                        code: "AS/NZS 3000:2018",
                        clause: "Clause 3.5.1",
                        summary: "Before adding any significant load to an existing installation, a maximum demand assessment must verify the supply capacity is sufficient. This applies to EV chargers, solar inverters, and other large fixed appliances."
                    ),
                    ContentBlock(
                        type: .callout,
                        content: "Many distributors offer a streamlined process for supply upgrades from 63A to 80A or 100A. Contact the local DNSP (e.g., Ausgrid, Endeavour, Essential Energy in NSW) before quoting the job — the supply upgrade timeline can affect the project schedule.",
                        style: "info"
                    )
                ],
                mode: .research
            )
        ],
        mode: .research
    )
}
