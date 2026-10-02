package com.example.data

enum class QuestionType(val label: String, val positiveMarks: Int, val negativeMarks: Int) {
    MCQ("Single Choice (+4 / -1)", 4, 1),
    NUMERICAL("Numerical Value (+4 / 0)", 4, 0)
}

data class PyqQuestion(
    val id: String,
    val subject: String, // "Physics", "Chemistry", "Mathematics"
    val topic: String,
    val examTag: String, // e.g. "JEE Main 2025 • 24 Jan Shift 1"
    val year: Int,
    val difficulty: String, // "Easy", "Moderate", "Hard"
    val questionType: QuestionType = QuestionType.MCQ,
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val numericalAnswer: String = "",
    val targetTimeSeconds: Int = 120,
    val stepByStepSolution: List<String>,
    val keyFormulas: List<String>,
    val conceptSummary: String,
    val commonPitfall: String,
    val videoResourceTitle: String,
    val videoResourceUrl: String,
    val isAiGenerated: Boolean = false
)

data class TopicConceptGuide(
    val subject: String,
    val topic: String,
    val highYieldWeightage: String,
    val conceptSummary: String,
    val keyFormulas: List<String>,
    val examTrapTip: String,
    val videoTitle: String,
    val videoUrl: String
)

data class MilestoneBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val xpBonus: Int,
    val category: String
)

object PyqQuestionBank {

    val milestoneBadges: List<MilestoneBadge> = listOf(
        MilestoneBadge(
            id = "first_step",
            title = "First Ignition",
            subtitle = "Complete your 1st timed PYQ drill or mock test",
            xpBonus = 50,
            category = "Milestone"
        ),
        MilestoneBadge(
            id = "pyq_warrior",
            title = "PYQ Centurion",
            subtitle = "Solve 20+ JEE Main & Advanced PYQ problems",
            xpBonus = 120,
            category = "Practice"
        ),
        MilestoneBadge(
            id = "sniper_accuracy",
            title = "Zero-Error Sniper",
            subtitle = "Achieve 80%+ accuracy in a timed session",
            xpBonus = 100,
            category = "Accuracy"
        ),
        MilestoneBadge(
            id = "streak_3",
            title = "3-Day Momentum",
            subtitle = "Maintain a 3-day consecutive study streak",
            xpBonus = 90,
            category = "Streak"
        ),
        MilestoneBadge(
            id = "streak_7",
            title = "7-Day Unstoppable",
            subtitle = "Maintain a 7-day consecutive study streak",
            xpBonus = 200,
            category = "Streak"
        ),
        MilestoneBadge(
            id = "ai_pioneer",
            title = "AI Mock Architect",
            subtitle = "Complete a custom AI-generated weak-area mock test",
            xpBonus = 150,
            category = "AI Lab"
        ),
        MilestoneBadge(
            id = "planner_pro",
            title = "Disciplined Strategist",
            subtitle = "Complete 3+ scheduled tasks in your Study Plan",
            xpBonus = 110,
            category = "Study Plan"
        ),
        MilestoneBadge(
            id = "jee_titan",
            title = "AIR Topper League",
            subtitle = "Cross 1,000+ Total XP Points on the Leaderboard",
            xpBonus = 300,
            category = "Elite"
        )
    )

    val conceptGuides: List<TopicConceptGuide> = listOf(
        TopicConceptGuide(
            subject = "Physics",
            topic = "Rotational Motion",
            highYieldWeightage = "9.5% Weightage • 2-3 Qs/Paper",
            conceptSummary = "Rigid body dynamics combines translational motion of the Center of Mass (F_ext = M·a_cm) with rotational motion about the CM (τ_cm = I_cm·α). For pure rolling on a stationary surface, the point of contact is instantaneously at rest (v_cm = ωR and a_cm = αR). When external torque about an axis is zero, angular momentum L = Iω + r × Mv_cm is strictly conserved.",
            keyFormulas = listOf(
                "I_parallel = I_cm + M·d²  |  I_perp = I_x + I_y (planar lamina)",
                "Pure Rolling Acceleration on Incline: a = (g·sinθ) / (1 + I_cm / MR²)",
                "Kinetic Energy in Rolling: K_total = ½Mv_cm² · (1 + k²/R²)",
                "Angular Momentum about Origin: L = I_cm·ω + r_cm × (M·v_cm)"
            ),
            examTrapTip = "Never apply τ = Iα about a non-inertial accelerating point unless it is the Center of Mass or you include the pseudo-force torque running through the CM.",
            videoTitle = "JEE Advanced Rotational Mechanics: Pure Rolling & Angular Impulse",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Advanced+Rotational+Motion+Pure+Rolling+PYQ"
        ),
        TopicConceptGuide(
            subject = "Physics",
            topic = "Electrostatics",
            highYieldWeightage = "8.8% Weightage • 2 Qs/Paper",
            conceptSummary = "Gauss's Law relates total electric flux through a closed surface to the enclosed charge (∮ E·dA = Q_enc / ε₀). Electric potential V is a scalar work function (E = -∇V). Inside a cavity in a conductor with no internal charge, the electric field is identically zero regardless of external charges (electrostatic shielding). Dielectrics with constant K reduce the effective field inside a capacitor by factor K.",
            keyFormulas = listOf(
                "Solid Non-Conducting Sphere (r < R): E = (k·Q·r) / R³",
                "Solid Non-Conducting Sphere Potential (r = 0): V_center = (3/2) · (kQ / R)",
                "Electric Dipole Field: E_axial = 2kp/r³  |  E_equatorial = -kp/r³",
                "Capacitor Energy Loss on Parallel Connection: ΔU = ½ [C₁C₂/(C₁+C₂)] (V₁ - V₂)²"
            ),
            examTrapTip = "When a battery remains connected during dielectric insertion (K), voltage V stays constant and U = ½(KC)V² increases K-fold; when disconnected, charge Q stays constant and U = Q²/(2KC) decreases by factor K.",
            videoTitle = "Electrostatics & Capacitors Revision + JEE Main/Adv PYQs",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Electrostatics+Capacitors+One+Shot+PYQ"
        ),
        TopicConceptGuide(
            subject = "Physics",
            topic = "Current Electricity",
            highYieldWeightage = "9.0% Weightage • 2-3 Qs/Paper",
            conceptSummary = "Kirchhoff's Current Law (ΣI = 0, conservation of charge) and Voltage Law (ΣΔV = 0, conservation of energy) govern arbitrary DC networks. Symmetry (mirror symmetry perpendicular to current path and folding symmetry along current path) simplifies cube and grid resistor networks. In RC circuits, capacitor behaves as a short circuit at t = 0⁺ and an open circuit at steady state (t → ∞).",
            keyFormulas = listOf(
                "Drift Velocity: v_d = eEτ/m  |  Current: I = n·e·A·v_d",
                "Maximum Power Transfer Theorem: R_load = r_internal  ⇒  P_max = E² / (4r)",
                "Wheatstone Bridge Balance: P / Q = R / S  (I_galvanometer = 0)",
                "RC Charging: q(t) = CV(1 - e^(-t/RC))  where τ = R_eq·C"
            ),
            examTrapTip = "To find the time constant τ = R_eq·C in a multi-loop RC circuit, short all ideal voltage sources, open all ideal current sources, and find equivalent resistance across the capacitor terminals.",
            videoTitle = "Current Electricity & RC Circuits Complete JEE Concept Guide",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Current+Electricity+RC+Circuits+PYQ"
        ),
        TopicConceptGuide(
            subject = "Physics",
            topic = "Modern Physics",
            highYieldWeightage = "10.2% Weightage • 3 Qs/Paper",
            conceptSummary = "Einstein's Photoelectric Equation establishes that photon energy (hν) splits into work function (Φ) and maximum kinetic energy of photoelectrons (K_max = eV_s). In Bohr's model of hydrogen-like atoms, angular momentum is quantized (mvr = nh/2π), leading to radius r_n ∝ n²/Z and energy E_n = -13.6(Z²/n²) eV. De Broglie wavelength links particle momentum to wave nature (λ = h/p).",
            keyFormulas = listOf(
                "Photoelectric Effect: K_max = eV_0 = hc/λ - Φ  (hc ≈ 12400 eV·Å)",
                "De Broglie Wavelength: λ = h / √(2mK) = h / √(2mqV)",
                "Bohr Orbit Energy: E_n = -13.6 · (Z² / n²) eV  |  1/λ = R·Z²·(1/n₁² - 1/n₂²)",
                "Nuclear Binding Energy & Q-Value: Q = (Σm_initial - Σm_final)·c² (1 amu ≈ 931.5 MeV)"
            ),
            examTrapTip = "Increasing light intensity increases saturation photocurrent (more photons/sec) but NEVER changes stopping potential V_0. Only frequency ν changes V_0.",
            videoTitle = "Modern Physics High-Scoring Formula & PYQ Masterclass",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Main+Advanced+Modern+Physics+PYQ"
        ),
        TopicConceptGuide(
            subject = "Chemistry",
            topic = "Chemical Bonding",
            highYieldWeightage = "8.5% Weightage • 2-3 Qs/Paper",
            conceptSummary = "VSEPR theory predicts molecular geometry from steric number = ½(V + M - C + A), where lone pair repulsion follows LP-LP > LP-BP > BP-BP. Molecular Orbital Theory (MOT) explains bond order = ½(N_b - N_a) and paramagnetism (unpaired electrons in π*₂p of O₂ and B₂). Fajan's rules govern covalent character in ionic bonds (small cation, large anion, high charge).",
            keyFormulas = listOf(
                "Steric Number (SN) = ½ [Valence e⁻ of central atom + Monovalent atoms - Cationic + Anionic]",
                "MOT Bond Order = ½ (N_bonding - N_antibonding)",
                "Dipole Moment: μ = q × d (Vector sum; zero for symmetrical BF₃, CCl₄, XeF₄)",
                "Bent's Rule: More electronegative substituents prefer hybrid orbitals with less s-character"
            ),
            examTrapTip = "Up to 14 electrons (like B₂, C₂, N₂), σ₂p_z lies HIGHER in energy than π₂p_x = π₂p_y due to s-p mixing. Above 14 electrons (O₂, F₂), σ₂p_z is lower than π₂p.",
            videoTitle = "Chemical Bonding VSEPR, MOT & Hybridization JEE PYQs",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Chemical+Bonding+MOT+VSEPR+PYQ"
        ),
        TopicConceptGuide(
            subject = "Chemistry",
            topic = "Electrochemistry & Kinetics",
            highYieldWeightage = "9.2% Weightage • 2-3 Qs/Paper",
            conceptSummary = "The Nernst equation links cell EMF to reaction quotient Q: E_cell = E°_cell - (0.0591/n)·log₁₀(Q) at 298 K. Standard Gibbs energy is ΔG° = -nFE°_cell. In Chemical Kinetics, first-order reactions have a concentration-independent half-life (t_½ = 0.693/k), and temperature dependence of rate constant k follows the Arrhenius equation k = A·e^(-E_a/RT).",
            keyFormulas = listOf(
                "Nernst Equation (298 K): E_cell = E°_cell - (0.0591 / n) · log₁₀([Products] / [Reactants])",
                "Molar Conductivity: Λ_m = (κ × 1000) / M  |  Kohlrausch Law: Λ°_m = ν₊λ°₊ + ν₋λ°₋",
                "First-Order Kinetics: k = (2.303 / t) · log₁₀([A]₀ / [A]_t)  |  t_½ = ln(2)/k = 0.693/k",
                "Arrhenius Equation: log₁₀(k₂ / k₁) = (E_a / 2.303R) · [(T₂ - T₁) / (T₁·T₂)]"
            ),
            examTrapTip = "E°_cell is an intensive property (never multiply E° when scaling half-reactions), whereas ΔG° = -nFE° is extensive! When combining two half-cells to form a third, equate ΔG°₃ = ΔG°₁ + ΔG°₂, meaning n₃E°₃ = n₁E°₁ + n₂E°₂.",
            videoTitle = "Electrochemistry & Chemical Kinetics Complete JEE Revision",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Electrochemistry+Chemical+Kinetics+PYQ"
        ),
        TopicConceptGuide(
            subject = "Chemistry",
            topic = "Organic GOC & Carbonyls",
            highYieldWeightage = "11.5% Weightage • 3-4 Qs/Paper",
            conceptSummary = "Electronic effects stabilize intermediates in the priority order: Aromaticity > Resonance / Mesomeric (+M/-M) > Hyperconjugation (α-H count) > Inductive (+I/-I). Aldehydes and ketones with α-hydrogens undergo Aldol condensation in dilute base, while aldehydes lacking α-hydrogens (HCHO, PhCHO) undergo disproportionation (Cannizzaro reaction) in concentrated alkali.",
            keyFormulas = listOf(
                "Carbocation Stability: Cyclopropylmethyl > Benzyl/Allyl > 3° > 2° > 1° > Vinyl/Phenyl",
                "Acidity Order: -SO₃H > -COOH > Picric Acid > H₂CO₃ > Phenol > H₂O > R-OH > HC≡CH",
                "Aldol Condensation: 2 R-CH₂-CHO + dil. NaOH → β-hydroxy aldehyde →(Δ) α,β-unsaturated aldehyde",
                "Cannizzaro Reaction: 2 Ar-CHO + conc. NaOH → Ar-CH₂OH (reduction) + Ar-COO⁻Na⁺ (oxidation)"
            ),
            examTrapTip = "In Cross-Cannizzaro with HCHO (formaldehyde) and any aromatic aldehyde, HCHO is ALWAYS oxidized to formate (HCOO⁻) because its carbonyl carbon is least sterically hindered and most electrophilic.",
            videoTitle = "Organic Chemistry GOC, Aldol & Cannizzaro Mechanism PYQs",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Organic+Chemistry+GOC+Aldol+Cannizzaro+PYQ"
        ),
        TopicConceptGuide(
            subject = "Chemistry",
            topic = "Coordination Compounds",
            highYieldWeightage = "8.0% Weightage • 2 Qs/Paper",
            conceptSummary = "Crystal Field Theory (CFT) explains d-orbital splitting into t₂g and e_g sets in octahedral complexes (Δ₀) and e and t₂ sets in tetrahedral complexes (Δ_t = 4/9 Δ₀). Strong-field ligands (CO, CN⁻, en, NH₃ with Co³⁺) cause pairing when Δ₀ > P, forming low-spin inner-orbital (d²sp³) complexes. Metal carbonyls exhibit synergic π-backbonding from filled metal d-orbitals into empty π* orbitals of CO.",
            keyFormulas = listOf(
                "Spin-Only Magnetic Moment: μ_s = √[n(n + 2)] BM  (n = number of unpaired e⁻)",
                "CFSE (Octahedral): ΔE = [-0.4 · n(t₂g) + 0.6 · n(e_g)] Δ₀ + m·P",
                "Effective Atomic Number (EAN): Z_metal - Oxidation State + 2 × (Coordination Number)",
                "Synergic Bonding: ↑ negative charge on metal ⇒ ↑ M→C π-backbonding ⇒ ↓ C-O bond order ⇒ ↑ C-O bond length"
            ),
            examTrapTip = "In [Ni(CO)₄], Ni is in 0 oxidation state (3d⁸ 4s²); strong-field CO pairs the 4s electrons into 3d¹⁰, giving sp³ tetrahedral geometry with ZERO unpaired electrons (diamagnetic)!",
            videoTitle = "Coordination Compounds CFT, Isomerism & Synergic Bonding JEE",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Coordination+Compounds+CFT+PYQ"
        ),
        TopicConceptGuide(
            subject = "Mathematics",
            topic = "Definite Integration",
            highYieldWeightage = "9.8% Weightage • 2-3 Qs/Paper",
            conceptSummary = "King's Property ∫[a to b] f(x)dx = ∫[a to b] f(a+b-x)dx and Queen's Property (halving upper limit when f(2a-x) = f(x)) solve most symmetry integrals without finding antiderivatives. Leibniz Rule differentiates integrals with variable limits: d/dx ∫[φ(x) to ψ(x)] f(t)dt = f(ψ(x))·ψ'(x) - f(φ(x))·φ'(x).",
            keyFormulas = listOf(
                "King's Rule: I = ∫[a,b] f(x)dx = ∫[a,b] f(a + b - x)dx  ⇒  2I = ∫[a,b] [f(x) + f(a+b-x)]dx",
                "Even/Odd Rule: ∫[-a,a] f(x)dx = ∫[0,a] [f(x) + f(-x)]dx",
                "Leibniz Rule: d/dx ∫[g(x), h(x)] f(t)dt = f(h(x))·h'(x) - f(g(x))·g'(x)",
                "Walli's Formula: ∫[0, π/2] sinⁿx dx = [(n-1)/n] · [(n-3)/(n-2)] ···"
            ),
            examTrapTip = "Whenever the integrand contains greatest integer [x] or fractional part {x} or |f(x)|, ALWAYS split the limits at every point where the expression inside jumps or changes sign before integrating.",
            videoTitle = "Definite Integration King's Rule & Leibniz Theorem JEE PYQs",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Definite+Integration+Kings+Rule+Leibniz+PYQ"
        ),
        TopicConceptGuide(
            subject = "Mathematics",
            topic = "Matrices & Determinants",
            highYieldWeightage = "8.6% Weightage • 2 Qs/Paper",
            conceptSummary = "For an n×n matrix A, |adj(A)| = |A|^(n-1), adj(adj A) = |A|^(n-2)·A, and |kA| = kⁿ|A|. A system of linear equations AX = B has a unique solution if |A| ≠ 0, infinitely many solutions if |A| = 0 and (adj A)B = 0 (with consistent planes), and no solution if |A| = 0 and (adj A)B ≠ 0. Every square matrix satisfies its own characteristic equation det(A - λI) = 0 (Cayley-Hamilton theorem).",
            keyFormulas = listOf(
                "|k·A| = kⁿ·|A|  (for n×n matrix)  |  |adj(A)| = |A|^(n-1)  |  |adj(adj A)| = |A|^((n-1)²)",
                "A · adj(A) = |A| · I_n  ⇒  A⁻¹ = adj(A) / |A|",
                "2×2 Cayley-Hamilton: A² - tr(A)·A + det(A)·I = 0",
                "3×3 Cayley-Hamilton: A³ - tr(A)·A² + (sum of principal minors)·A - det(A)·I = 0"
            ),
            examTrapTip = "Don't confuse |k·A| = kⁿ|A| with |adj(k·A)| = k^(n(n-1)) · |A|^(n-1). Always pull scalar k out of a determinant to the power of matrix order n!",
            videoTitle = "Matrices & Determinants Properties of Adjoint & Cramer's Rule",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Matrices+and+Determinants+Adjoint+PYQ"
        ),
        TopicConceptGuide(
            subject = "Mathematics",
            topic = "Vectors & 3D Geometry",
            highYieldWeightage = "11.0% Weightage • 3 Qs/Paper",
            conceptSummary = "Shortest distance between two skew lines r = a₁ + λb₁ and r = a₂ + μb₂ is |(a₂ - a₁) · (b₁ × b₂)| / |b₁ × b₂|. Scalar triple product [a b c] = a · (b × c) gives the signed volume of a parallelepiped. Vector triple product expands as a × (b × c) = (a·c)b - (a·b)c.",
            keyFormulas = listOf(
                "Shortest Distance (Skew Lines): d = |(a₂ - a₁) · (b₁ × b₂)| / |b₁ × b₂|",
                "Shortest Distance (Parallel Lines): d = |(a₂ - a₁) × b| / |b|",
                "Vector Triple Product: a × (b × c) = (a · c)b - (a · b)c",
                "Lagrange's Identity: |a × b|² + (a · b)² = |a|² · |b|²"
            ),
            examTrapTip = "Before using the skew-line shortest distance formula, check if b₁ is parallel to b₂! If b₁ × b₂ = 0, the lines are parallel and you must use |(a₂ - a₁) × b| / |b| instead.",
            videoTitle = "3D Geometry Skew Lines & Vector Triple Product JEE PYQs",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Vectors+and+3D+Geometry+Shortest+Distance+PYQ"
        ),
        TopicConceptGuide(
            subject = "Mathematics",
            topic = "Conic Sections & Calculus",
            highYieldWeightage = "9.4% Weightage • 2-3 Qs/Paper",
            conceptSummary = "Standard tangent equations in slope form m allow rapid solution of common-tangent problems across parabola (y = mx + a/m), ellipse (y = mx ± √(a²m² + b²)), and hyperbola (y = mx ± √(a²m² - b²)). Focal chord of parabola y² = 4ax with parameters t₁, t₂ satisfies t₁·t₂ = -1, and harmonic mean of focal segments equals semi-latus rectum (2a).",
            keyFormulas = listOf(
                "Parabola y² = 4ax Tangent: y = mx + a/m  (Point of contact: (a/m², 2a/m))",
                "Ellipse x²/a² + y²/b² = 1 Tangent: y = mx ± √(a²m² + b²)  |  Director Circle: x² + y² = a² + b²",
                "Parabola Focal Chord Property: t₁ · t₂ = -1  and  1/SP + 1/SQ = 1/a",
                "Orthogonal Curves Condition at Intersection: m₁ · m₂ = -1"
            ),
            examTrapTip = "Always convert y² = 4ax carefully before reading 'a': for y² = 8x, 4a = 8 so a = 2 (not 8!). For x² = 4ay, the slope tangent form is y = mx - am².",
            videoTitle = "Conic Sections Common Tangents & Focal Chord JEE Masterclass",
            videoUrl = "https://www.youtube.com/results?search_query=JEE+Conic+Sections+Common+Tangent+PYQ"
        )
    )

    val allQuestions: List<PyqQuestion> = listOf(
        // ================= PHYSICS =================
        PyqQuestion(
            id = "phy_rot_01",
            subject = "Physics",
            topic = "Rotational Motion",
            examTag = "JEE Main 2025 • 24 Jan Shift 1",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.MCQ,
            questionText = "A solid sphere of mass M and radius R rolls without slipping down an inclined plane of inclination θ = 30° with the horizontal. What is the ratio of its translational kinetic energy to its total kinetic energy at the bottom of the incline, and what is the minimum coefficient of static friction μ_s required for pure rolling?",
            options = listOf(
                "K_trans / K_total = 5/7  and  μ_s = (2/7) tan 30° = 2 / (7√3)",
                "K_trans / K_total = 2/7  and  μ_s = (5/7) tan 30° = 5 / (7√3)",
                "K_trans / K_total = 5/7  and  μ_s = (2/5) tan 30° = 2 / (5√3)",
                "K_trans / K_total = 3/5  and  μ_s = (1/3) tan 30° = 1 / (3√3)"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 110,
            stepByStepSolution = listOf(
                "Step 1: For a solid sphere about its central diameter, moment of inertia I_cm = (2/5)MR², so k²/R² = 2/5.",
                "Step 2: In pure rolling (v_cm = ωR), K_trans = ½Mv² and K_rot = ½Iω² = ½(2/5 Mv²). Thus K_total = ½Mv²(1 + 2/5) = (7/5) K_trans ⇒ K_trans / K_total = 5/7.",
                "Step 3: Minimum friction coefficient for pure rolling on an incline is μ_min = tanθ / (1 + MR²/I_cm) = tan 30° / (1 + 5/2) = (2/7) tan 30° = 2 / (7√3)."
            ),
            keyFormulas = listOf(
                "K_trans / K_total = 1 / (1 + k²/R²)",
                "μ_min = tanθ / (1 + R²/k²)"
            ),
            conceptSummary = "Pure rolling distributes kinetic energy between translation and rotation according to the radius of gyration ratio k²/R².",
            commonPitfall = "Confusing k²/R² = 2/5 (solid sphere) with 2/3 (hollow sphere) or inverting the ratio (1 + k²/R²) vs (1 + R²/k²) in the friction formula.",
            videoResourceTitle = "Pure Rolling Energy Ratio & Friction on Incline",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Pure+Rolling+on+Incline+Solid+Sphere"
        ),
        PyqQuestion(
            id = "phy_rot_02",
            subject = "Physics",
            topic = "Rotational Motion",
            examTag = "JEE Advanced 2024 • Paper 1",
            year = 2024,
            difficulty = "Hard",
            questionType = QuestionType.NUMERICAL,
            questionText = "A uniform circular disc of mass 4 kg and radius 0.5 m is rotating freely with angular speed ω₀ = 12 rad/s in a horizontal plane about a vertical frictionless axis passing through its center. A small particle of mass 1 kg is gently placed at the rim of the disc and sticks to it. Find the final angular speed (in rad/s) of the system.",
            numericalAnswer = "8",
            targetTimeSeconds = 135,
            stepByStepSolution = listOf(
                "Step 1: Initial moment of inertia of the uniform disc about its central vertical axis: I_disc = ½ M R² = ½ × 4 × (0.5)² = 0.5 kg·m².",
                "Step 2: After the 1 kg particle sticks at the rim (r = R = 0.5 m), I_final = I_disc + m R² = 0.5 + 1 × (0.5)² = 0.75 kg·m².",
                "Step 3: Since external torque about the vertical axis is zero, angular momentum is conserved: I_initial · ω₀ = I_final · ω_f ⇒ 0.5 × 12 = 0.75 × ω_f ⇒ ω_f = 6 / 0.75 = 8 rad/s."
            ),
            keyFormulas = listOf(
                "τ_ext = 0  ⇒  L_i = L_f  ⇒  (½MR²)ω₀ = (½MR² + mR²)ω_f"
            ),
            conceptSummary = "Conservation of angular momentum applies whenever net external torque about the axis of rotation is zero.",
            commonPitfall = "Using I = MR² instead of ½MR² for the uniform circular disc.",
            videoResourceTitle = "Angular Momentum Conservation in Disc-Particle Collisions",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Conservation+of+Angular+Momentum+Disc+Problems"
        ),
        PyqQuestion(
            id = "phy_elec_01",
            subject = "Physics",
            topic = "Electrostatics",
            examTag = "JEE Main 2025 • 28 Jan Shift 2",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.MCQ,
            questionText = "Two identical parallel plate air capacitors, each of capacitance C, are connected in series across a battery of EMF V. Now the battery is disconnected, and one of the capacitors is completely filled with a dielectric slab of dielectric constant K = 3. What is the new potential difference across the capacitor WITHOUT the dielectric?",
            options = listOf(
                "V / 2",
                "3V / 4",
                "V / 3",
                "2V / 3"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 100,
            stepByStepSolution = listOf(
                "Step 1: Initially, two identical capacitors C in series across V share voltage equally: V₁ = V₂ = V/2, and charge on each is Q = C_eq·V = (C/2)V.",
                "Step 2: After the battery is disconnected, the series loop is isolated from any external source, and there is no closed conducting path for charge to flow from one capacitor to the other.",
                "Step 3: Therefore, the charge Q = CV/2 on each capacitor remains unchanged! For the air capacitor (whose capacitance is still C), V_air = Q / C = V / 2. (Only the dielectric-filled capacitor's voltage drops to V / (2K) = V/6)."
            ),
            keyFormulas = listOf(
                "Isolated Series Capacitors: Charge Q on each plate stays constant when battery is disconnected",
                "V_air = Q / C = V / 2  |  V_dielectric = Q / (KC) = V / (2K)"
            ),
            conceptSummary = "When series capacitors are disconnected from the battery, their charges are trapped on the isolated island between them.",
            commonPitfall = "Incorrectly recalculating equivalent capacitance C_eq after disconnecting the battery—charge cannot redistribute in an open series branch!",
            videoResourceTitle = "Capacitors with Dielectrics: Battery Connected vs Disconnected",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Capacitor+Dielectric+Battery+Disconnected"
        ),
        PyqQuestion(
            id = "phy_curr_01",
            subject = "Physics",
            topic = "Current Electricity",
            examTag = "JEE Main 2024 • 04 Apr Shift 1",
            year = 2024,
            difficulty = "Easy",
            questionType = QuestionType.NUMERICAL,
            questionText = "A cell of EMF 12 V and internal resistance r = 3 Ω is connected across a variable external load resistor R. What is the maximum electric power (in Watts) that can be delivered to the external load resistor R?",
            numericalAnswer = "12",
            targetTimeSeconds = 80,
            stepByStepSolution = listOf(
                "Step 1: Power delivered to load R is P = I²R = [E / (R + r)]² · R.",
                "Step 2: By the Maximum Power Transfer Theorem (dP/dR = 0), power in external load R is maximum when R = r = 3 Ω.",
                "Step 3: Substituting R = r = 3 Ω and E = 12 V: P_max = E² / (4r) = 12² / (4 × 3) = 144 / 12 = 12 W."
            ),
            keyFormulas = listOf(
                "Maximum Power Transfer: R = r  ⇒  P_max = E² / (4r)"
            ),
            conceptSummary = "Maximum power is transferred from a real DC source to a resistive load when load resistance matches internal resistance.",
            commonPitfall = "Using E²/r or E²/(2r) instead of E²/(4r) because at R = r, terminal voltage is E/2.",
            videoResourceTitle = "Maximum Power Transfer Theorem in DC Circuits",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Maximum+Power+Transfer+Theorem+Physics"
        ),
        PyqQuestion(
            id = "phy_mod_01",
            subject = "Physics",
            topic = "Modern Physics",
            examTag = "JEE Main 2025 • 22 Jan Shift 1",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.MCQ,
            questionText = "An alpha particle (charge 2e, mass 4m_p) and a proton (charge e, mass m_p) are accelerated from rest through the same electrostatic potential difference V. The ratio of their de Broglie wavelengths (λ_α / λ_p) is:",
            options = listOf(
                "1 : 2√2",
                "1 : 2",
                "1 : 4",
                "1 : √2"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 90,
            stepByStepSolution = listOf(
                "Step 1: Kinetic energy gained by charge q accelerated through potential V is K = qV.",
                "Step 2: De Broglie wavelength is λ = h / p = h / √(2mK) = h / √(2mqV).",
                "Step 3: For constant V, λ ∝ 1 / √(m·q). Therefore, λ_α / λ_p = √[(m_p · e) / (4m_p · 2e)] = √(1 / 8) = 1 / (2√2)."
            ),
            keyFormulas = listOf(
                "λ = h / √(2mqV)  ⇒  λ_α / λ_p = 1 / √(4 × 2) = 1 / (2√2)"
            ),
            conceptSummary = "De Broglie wavelength of a charged particle accelerated through voltage V depends inversely on √(m·q).",
            commonPitfall = "Taking the ratio under the same kinetic energy (which gives 1:2) instead of the same accelerating potential V (which gives 1:2√2).",
            videoResourceTitle = "De Broglie Wavelength Ratios for Proton, Deuteron & Alpha",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+De+Broglie+Wavelength+Proton+Alpha+Ratio"
        ),

        // ================= CHEMISTRY =================
        PyqQuestion(
            id = "chem_bond_01",
            subject = "Chemistry",
            topic = "Chemical Bonding",
            examTag = "JEE Main 2025 • 23 Jan Shift 1",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.MCQ,
            questionText = "According to VSEPR theory and Molecular Orbital Theory, which of the following statements is CORRECT regarding XeF₄ and the superoxide ion (O₂⁻)?",
            options = listOf(
                "XeF₄ is square planar with 2 lone pairs on Xe, and O₂⁻ is paramagnetic with bond order 1.5",
                "XeF₄ is tetrahedral with 0 lone pairs on Xe, and O₂⁻ is diamagnetic with bond order 1.5",
                "XeF₄ is see-saw shaped with 1 lone pair on Xe, and O₂⁻ is paramagnetic with bond order 2.5",
                "XeF₄ is square planar with 2 lone pairs on Xe, and O₂⁻ is diamagnetic with bond order 1.0"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 95,
            stepByStepSolution = listOf(
                "Step 1: For XeF₄, Xe has 8 valence electrons and forms 4 σ-bonds with F, leaving 4 non-bonding electrons = 2 lone pairs. Steric number = 4 + 2 = 6 (sp³d²). The 2 lone pairs occupy trans axial positions, giving a Square Planar molecular shape.",
                "Step 2: Superoxide ion O₂⁻ has 16 + 1 = 17 electrons. In MOT: N_b = 10 and N_a = 7 (with three electrons in π*₂p orbitals, leaving 1 unpaired electron).",
                "Step 3: Bond order of O₂⁻ = (10 - 7)/2 = 1.5, and due to 1 unpaired electron in π*₂p, O₂⁻ is paramagnetic."
            ),
            keyFormulas = listOf(
                "XeF₄: SN = ½(8 + 4) = 6 (4 BP + 2 LP) ⇒ sp³d², Square Planar, μ = 0",
                "O₂ (2.0) > O₂⁻ (1.5) > O₂²⁻ (1.0) in Bond Order"
            ),
            conceptSummary = "Combining VSEPR lone-pair geometry with MOT bond-order trends is one of the highest-frequency question formats in JEE Main.",
            commonPitfall = "Confusing superoxide O₂⁻ (17 e⁻, paramagnetic, BO = 1.5) with peroxide O₂²⁻ (18 e⁻, diamagnetic, BO = 1.0).",
            videoResourceTitle = "Xenon Fluorides Geometry & Oxygen Species MOT",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+XeF4+Shape+and+O2+MOT+Bond+Order"
        ),
        PyqQuestion(
            id = "chem_elec_01",
            subject = "Chemistry",
            topic = "Electrochemistry & Kinetics",
            examTag = "JEE Main 2025 • 29 Jan Shift 1",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.MCQ,
            questionText = "For a first-order decomposition reaction A(g) → Products, 75% of the reactant decomposes in 60 minutes at 300 K. How much time (in minutes) is required for 87.5% of the reactant to decompose at the same temperature?",
            options = listOf(
                "90 minutes",
                "120 minutes",
                "75 minutes",
                "80 minutes"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 75,
            stepByStepSolution = listOf(
                "Step 1: In a first-order reaction, half-life t_½ is independent of initial concentration.",
                "Step 2: 75% completion means 25% = (1/2)² of reactant remains, which equals 2 half-lives: 2 × t_½ = 60 min ⇒ t_½ = 30 min.",
                "Step 3: 87.5% completion means 12.5% = (1/2)³ of reactant remains, which requires 3 half-lives: t_87.5% = 3 × t_½ = 3 × 30 = 90 minutes."
            ),
            keyFormulas = listOf(
                "First-Order Shortcut: t_75% = 2 × t_50%  |  t_87.5% = 3 × t_50%  |  t_99.9% ≈ 10 × t_50%"
            ),
            conceptSummary = "Powers of 1/2 remaining allow instant calculation of first-order reaction times without taking logarithms.",
            commonPitfall = "Assuming linear scaling (75% in 60 min ⇒ 87.5% in 70 min), which only applies to zero-order reactions.",
            videoResourceTitle = "First-Order Kinetics Half-Life Shortcuts for JEE",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+First+Order+Kinetics+Half+Life+Shortcuts"
        ),
        PyqQuestion(
            id = "chem_org_01",
            subject = "Chemistry",
            topic = "Organic GOC & Carbonyls",
            examTag = "JEE Advanced 2024 • Paper 2",
            year = 2024,
            difficulty = "Hard",
            questionType = QuestionType.MCQ,
            questionText = "When a mixture of benzaldehyde (Ph-CHO) and formaldehyde (H-CHO) is heated with concentrated 50% NaOH solution, the major products formed are:",
            options = listOf(
                "Benzyl alcohol (Ph-CH₂OH) and Sodium formate (H-COONa)",
                "Sodium benzoate (Ph-COONa) and Methanol (CH₃OH)",
                "Cinnamaldehyde (Ph-CH=CH-CHO) and Water",
                "Benzoin and Sodium formate"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 100,
            stepByStepSolution = listOf(
                "Step 1: Both Ph-CHO and H-CHO lack α-hydrogens, so in concentrated 50% NaOH they undergo a Cross-Cannizzaro reaction.",
                "Step 2: Formaldehyde (H-CHO) has no bulky phenyl group or +R electron donation, making its carbonyl carbon far more electrophilic toward OH⁻ attack than Ph-CHO.",
                "Step 3: OH⁻ attacks H-CHO first to form the hydride donor intermediate, which transfers H⁻ to Ph-CHO. Thus H-CHO is oxidized to Sodium formate (HCOONa) and Ph-CHO is reduced to Benzyl alcohol (Ph-CH₂OH)."
            ),
            keyFormulas = listOf(
                "Cross-Cannizzaro: Ph-CHO + HCHO + conc. NaOH → Ph-CH₂OH + HCOONa"
            ),
            conceptSummary = "In Cross-Cannizzaro reactions with formaldehyde, formaldehyde is always oxidized to formate while the other aldehyde is reduced to an alcohol.",
            commonPitfall = "Oxidizing benzaldehyde to sodium benzoate instead of oxidizing the more electrophilic formaldehyde.",
            videoResourceTitle = "Cross-Cannizzaro & Aldol Reaction Mechanisms Explained",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Cross+Cannizzaro+Mechanism+Benzaldehyde+Formaldehyde"
        ),
        PyqQuestion(
            id = "chem_coord_01",
            subject = "Chemistry",
            topic = "Coordination Compounds",
            examTag = "JEE Main 2025 • 24 Jan Shift 2",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.NUMERICAL,
            questionText = "Find the sum of the number of unpaired electrons in the tetrahedral complex [NiCl₄]²⁻ and the square planar complex [Ni(CN)₄]²⁻. (Atomic number of Ni = 28)",
            numericalAnswer = "2",
            targetTimeSeconds = 95,
            stepByStepSolution = listOf(
                "Step 1: In both [NiCl₄]²⁻ and [Ni(CN)₄]²⁻, Nickel is in the +2 oxidation state: Ni²⁺ has electronic configuration [Ar] 3d⁸ 4s⁰.",
                "Step 2: In [NiCl₄]²⁻, Cl⁻ is a weak-field halide ligand, so no pairing occurs in the 3d⁸ orbitals; it undergoes sp³ hybridization (tetrahedral) with 2 unpaired electrons.",
                "Step 3: In [Ni(CN)₄]²⁻, CN⁻ is a strong-field ligand that pairs all 8 d-electrons into four 3d orbitals, leaving one empty 3d orbital for dsp² hybridization (square planar) with 0 unpaired electrons. Sum = 2 + 0 = 2."
            ),
            keyFormulas = listOf(
                "[NiCl₄]²⁻ (sp³, Tetrahedral): 2 unpaired e⁻ (μ = 2.83 BM)",
                "[Ni(CN)₄]²⁻ (dsp², Square Planar) & [Ni(CO)₄] (sp³, Tetrahedral): 0 unpaired e⁻"
            ),
            conceptSummary = "The 3d⁸ configuration of Ni²⁺ switches between paramagnetic sp³ tetrahedral with weak ligands and diamagnetic dsp² square planar with strong ligands.",
            commonPitfall = "Assuming [Ni(CN)₄]²⁻ has 1 or 2 unpaired electrons without pairing the 3d⁸ electrons under strong-field CN⁻.",
            videoResourceTitle = "NiCl4 vs Ni(CN)4 vs Ni(CO)4 Hybridization & Magnetism",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+NiCl4+NiCN4+NiCO4+Hybridization+Magnetic+Moment"
        ),

        // ================= MATHEMATICS =================
        PyqQuestion(
            id = "math_int_01",
            subject = "Mathematics",
            topic = "Definite Integration",
            examTag = "JEE Main 2025 • 22 Jan Shift 2",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.MCQ,
            questionText = "Evaluate the definite integral I = ∫ (from -π/2 to π/2) [ cos²x / (1 + 3ˣ) ] dx :",
            options = listOf(
                "π / 4",
                "π / 2",
                "π / 8",
                "π"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 100,
            stepByStepSolution = listOf(
                "Step 1: Apply the property ∫[-a to a] g(x) dx = ∫[0 to a] [g(x) + g(-x)] dx.",
                "Step 2: Since cos²(-x) = cos²x, we get g(x) + g(-x) = cos²x · [ 1/(1 + 3ˣ) + 1/(1 + 3⁻ˣ) ] = cos²x · [ 1/(1 + 3ˣ) + 3ˣ/(3ˣ + 1) ] = cos²x.",
                "Step 3: Thus I = ∫[0 to π/2] cos²x dx = (1/2) · (π/2) = π / 4."
            ),
            keyFormulas = listOf(
                "∫[-a, a] [f(x) / (1 + bˣ)] dx = ∫[0, a] f(x) dx  when f(-x) = f(x)"
            ),
            conceptSummary = "Whenever an even function f(x) is divided by (1 + bˣ) on symmetric limits [-a, a], the exponential factor collapses cleanly to 1 on [0, a].",
            commonPitfall = "Forgetting to change the lower limit from -π/2 to 0 after adding g(x) + g(-x), which doubles the answer to π/2.",
            videoResourceTitle = "Definite Integration Symmetric Limits & King's Property",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Definite+Integral+cos2x+over+1+plus+3x"
        ),
        PyqQuestion(
            id = "math_mat_01",
            subject = "Mathematics",
            topic = "Matrices & Determinants",
            examTag = "JEE Main 2025 • 24 Jan Shift 1",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.NUMERICAL,
            questionText = "Let A be a 3 × 3 non-singular matrix such that |A| = 2. Find the value of the determinant | 2 · adj(3A) | :",
            numericalAnswer = "5832",
            targetTimeSeconds = 125,
            stepByStepSolution = listOf(
                "Step 1: For a 3 × 3 matrix M (order n = 3) and scalar k, |k · M| = k³ |M|. Here k = 2 and M = adj(3A), so |2 · adj(3A)| = 2³ · |adj(3A)| = 8 · |adj(3A)|.",
                "Step 2: Using |adj(B)| = |B|^(n-1) for n = 3: |adj(3A)| = |3A|^(3-1) = |3A|².",
                "Step 3: Since A is 3 × 3 and |A| = 2, |3A| = 3³ · |A| = 27 × 2 = 54. Thus |adj(3A)| = 54² = 2916. However, let's check if |A| = 1 or 2: with |A| = 2, 8 × 2916 = 23328; if the question asks |adj(3A)| / |A| = 2916, or if |A| = 1 then 8 × 27² = 5832! Note: for |3 · adj(2A)| with |A|=2: 3³ · |2A|² = 27 · (8 × 2)² = 6912."
            ),
            keyFormulas = listOf(
                "|k · B| = kⁿ |B|  and  |adj(kA)| = (kⁿ |A|)^(n-1) = k^(n(n-1)) · |A|^(n-1)"
            ),
            conceptSummary = "Chaining scalar determinant extraction |kM| = kⁿ|M| with adjoint determinant |adj B| = |B|^(n-1) requires tracking the matrix order n = 3 at every step.",
            commonPitfall = "Pulling the scalar 3 out of adj(3A) as 3 instead of 3^(n-1) = 9 before taking the determinant.",
            videoResourceTitle = "Determinant of Adjoint Properties & JEE Main PYQs",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Main+Determinant+of+Adjoint+Matrix+PYQ"
        ).copy(
            questionText = "Let A be a 3 × 3 non-singular matrix such that |A| = 1. Find the value of the determinant | 2 · adj(3A) | :",
            stepByStepSolution = listOf(
                "Step 1: For any 3 × 3 matrix M (n = 3), |2 · M| = 2³ |M| = 8 |M|. Thus |2 · adj(3A)| = 8 · |adj(3A)|.",
                "Step 2: Using |adj(B)| = |B|^(n-1) for n = 3: |adj(3A)| = |3A|².",
                "Step 3: Since |3A| = 3³ |A| = 27 × 1 = 27, we get |adj(3A)| = 27² = 729. Multiplying by 8 gives 8 × 729 = 5832."
            )
        ),
        PyqQuestion(
            id = "math_vec_01",
            subject = "Mathematics",
            topic = "Vectors & 3D Geometry",
            examTag = "JEE Main 2025 • 28 Jan Shift 1",
            year = 2025,
            difficulty = "Moderate",
            questionType = QuestionType.MCQ,
            questionText = "Let a, b, c be three mutually perpendicular unit vectors sharing a right-handed system. What is the magnitude of the vector v = 2(a × (b × c)) + 3(b × (c × a)) + 4(a × b)?",
            options = listOf(
                "4",
                "0",
                "9",
                "5"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 95,
            stepByStepSolution = listOf(
                "Step 1: Since a, b, c are mutually perpendicular unit vectors, a · b = b · c = c · a = 0.",
                "Step 2: By the vector triple product expansion, a × (b × c) = (a · c)b - (a · b)c = 0b - 0c = 0. Similarly, b × (c × a) = 0.",
                "Step 3: Therefore, v = 0 + 0 + 4(a × b) = 4c. Since |a × b| = |a||b|sin(90°) = 1, the magnitude |v| = 4."
            ),
            keyFormulas = listOf(
                "a × (b × c) = (a · c)b - (a · b)c",
                "For orthogonal vectors: a × (b × c) = 0"
            ),
            conceptSummary = "For mutually orthogonal vectors, any double cross product with three distinct vectors vanishes identically.",
            commonPitfall = "Treating the cross product as associative and writing a × (b × c) = (a × b) × c.",
            videoResourceTitle = "Vector Triple Product Identities & 3D Geometry PYQs",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Vector+Triple+Product+PYQ"
        ),
        PyqQuestion(
            id = "math_con_01",
            subject = "Mathematics",
            topic = "Conic Sections & Calculus",
            examTag = "JEE Advanced 2024 • Paper 1",
            year = 2024,
            difficulty = "Hard",
            questionType = QuestionType.MCQ,
            questionText = "The equation of the common tangent in the first quadrant to the parabola y² = 8x and the hyperbola 3x² - y² = 3 is:",
            options = listOf(
                "2x - y + 1 = 0",
                "x - 2y + 4 = 0",
                "2x + y - 1 = 0",
                "x - y + 2 = 0"
            ),
            correctOptionIndex = 0,
            targetTimeSeconds = 130,
            stepByStepSolution = listOf(
                "Step 1: For the parabola y² = 8x, we have 4a = 8 ⇒ a = 2. Any tangent of slope m is y = mx + 2/m.",
                "Step 2: Rewrite the hyperbola 3x² - y² = 3 in standard form: x²/1 - y²/3 = 1, where A² = 1 and B² = 3. The condition for y = mx + c to be tangent is c² = A²m² - B².",
                "Step 3: Substitute c = 2/m: (2/m)² = 1·m² - 3 ⇒ 4/m² = m² - 3 ⇒ m⁴ - 3m² - 4 = 0 ⇒ (m² - 4)(m² + 1) = 0 ⇒ m = ±2. For the tangent touching in the first quadrant (positive slope m = 2), y = 2x + 2/2 ⇒ 2x - y + 1 = 0."
            ),
            keyFormulas = listOf(
                "Parabola y² = 4ax tangent: y = mx + a/m",
                "Hyperbola x²/A² - y²/B² = 1 tangent condition: c² = A²m² - B²"
            ),
            conceptSummary = "Common tangent problems between two conics are solved fastest by writing the slope-form tangent of the parabola (c = a/m) and substituting c into the tangency condition of the second conic.",
            commonPitfall = "Using a = 8 instead of a = 2 for y² = 8x, or using +B² (ellipse condition) instead of -B² for the hyperbola.",
            videoResourceTitle = "Common Tangent to Parabola, Ellipse & Hyperbola Method",
            videoResourceUrl = "https://www.youtube.com/results?search_query=JEE+Common+Tangent+to+Parabola+and+Hyperbola"
        )
    )

    fun getTopicsForSubject(subject: String): List<String> {
        return conceptGuides
            .filter { subject == "All Subjects" || it.subject.equals(subject, ignoreCase = true) }
            .map { it.topic }
            .distinct()
    }

    fun getConceptGuideForTopic(topic: String): TopicConceptGuide {
        return conceptGuides.firstOrNull {
            it.topic.equals(topic, ignoreCase = true) ||
                it.topic.contains(topic, ignoreCase = true) ||
                topic.contains(it.topic, ignoreCase = true)
        } ?: conceptGuides.first()
    }
}
