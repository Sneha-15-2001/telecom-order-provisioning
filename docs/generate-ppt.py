"""Generates Telecom-Order-Provisioning-Intro.pptx (run: python3 docs/generate-ppt.py)."""
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_CONNECTOR
from pptx.enum.text import PP_ALIGN
from pptx.util import Emu, Inches, Pt

RED = RGBColor(0xE4, 0x00, 0x00)
NAVY = RGBColor(0x0B, 0x1E, 0x4B)
DARK = RGBColor(0x20, 0x20, 0x20)
GRAY = RGBColor(0x66, 0x66, 0x66)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
LIGHT = RGBColor(0xF5, 0xF5, 0xF5)

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
BLANK = prs.slide_layouts[6]


def bg(slide, color):
    fill = slide.background.fill
    fill.solid()
    fill.fore_color.rgb = color


def box(slide, left, top, width, height, fill_color, line_color=None):
    shape = slide.shapes.add_shape(1, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = fill_color
    shape.line.fill.background()
    if line_color:
        shape.line.color.rgb = line_color
        shape.line.width = Pt(1.5)
    return shape


def text(shape, runs, size=18, bold=False, color=DARK, align=PP_ALIGN.LEFT):
    tf = shape.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.alignment = align
    if isinstance(runs, str):
        runs = [runs]
    for i, r in enumerate(runs):
        run = p.add_run() if i else p.runs[0] if p.runs else p.add_run()
        run.text = r
        run.font.size = Pt(size)
        run.font.bold = bold
        run.font.color.rgb = color
    return shape


def title_slide(title, subtitle, red_banner=True):
    s = prs.slides.add_slide(BLANK)
    bg(s, NAVY)
    if red_banner:
        box(s, Inches(0), Inches(0), Inches(13.333), Inches(0.12), RED)
    text(box(s, Inches(0.8), Inches(1.6), Inches(11.7), Inches(1.6), NAVY),
         title, size=44, bold=True, color=WHITE)
    text(box(s, Inches(0.8), Inches(3.4), Inches(11.7), Inches(2.5), NAVY),
         subtitle, size=22, color=RGBColor(0xD7, 0xDE, 0xF0))
    return s


def section_slide(kicker, title, bullets):
    s = prs.slides.add_slide(BLANK)
    bg(s, WHITE)
    box(s, Inches(0), Inches(0), Inches(13.333), Inches(0.12), RED)
    text(box(s, Inches(0.8), Inches(0.5), Inches(11.7), Inches(0.6), WHITE),
         kicker, size=16, bold=True, color=RED)
    text(box(s, Inches(0.8), Inches(1.1), Inches(11.7), Inches(1.0), WHITE),
         title, size=36, bold=True, color=NAVY)
    for i, b in enumerate(bullets):
        row = i // 2
        col = i % 2
        shape = box(s, Inches(0.8 + col * 6.1), Inches(2.5 + row * 1.15),
                    Inches(5.9), Inches(0.95), LIGHT)
        text(shape, b, size=16, color=DARK)
    return s


# 1. Title
title_slide("Telecom Order Provisioning System",
            "Airtel/Jio-style order journey — 5 Spring Boot microservices, Angular + React UIs,\n"
            "128 REST APIs, end-to-end fulfillment saga, reproducible incidents & an AI investigator.\nBuilt from scratch · Java 17 · Python · PostgreSQL")

# 2. WHAT
section_slide("WHAT — Introduction",
              "What is this project?",
              ["A mini-telecom backend: customers order SIMs, phones & fiber — the system validates, bills, reserves stock, activates & texts them.",
               "5 independent microservices, each with its own database — no shared tables, only REST calls with a tracing ID.",
               "Two frontends: NexaTel storefront (Angular, dark/light, carousel) + AI investigator console (React).",
               "10 reproducible production incidents (Jira-style tickets) + log-traced evidence for every one."])

# 3. WHY
section_slide("WHY — Problem & Purpose",
              "Why was it built?",
              ["Learn microservices the real way: ownership, contracts, failure, compensation — not toy CRUD.",
               "Rehearse production reality: stuck orders, leaked holds, N+1 queries, duplicate SMS — then investigate like on-call.",
               "Showcase an AI investigator: rule engine + LLM reasoning over live logs and databases.",
               "Portfolio + hackathon demo: one click runs the whole telecom story, including the rescue paths."])

# 4. The five services
section_slide("WHERE — System Map",
              "Five services, five databases",
              ["customer :8081 — people, addresses, subscriptions, eligibility checks.",
               "order :8082 — the brain: lifecycle, payments, promos, fulfill saga.",
               "inventory :8083 — SIMs, numbers, devices, fiber ports + holds.",
               "provisioning :8084 — simulated activation (mobile/eSIM/fiber/roaming).",
               "notification :8085 — simulated SMS/email + templates & retries.",
               "UIs: NexaTel :4200 (Angular) · Investigator :5173 (React) · AI API :8090 (Python)."])

# 5. FLOWCHART
s = prs.slides.add_slide(BLANK)
bg(s, WHITE)
box(s, Inches(0), Inches(0), Inches(13.333), Inches(0.12), RED)
text(box(s, Inches(0.8), Inches(0.5), Inches(11.7), Inches(0.6), WHITE),
     "HOW — Order Flowchart", size=16, bold=True, color=RED)
text(box(s, Inches(0.8), Inches(1.1), Inches(11.7), Inches(0.8), WHITE),
     "One connection, eight steps", size=32, bold=True, color=NAVY)
steps = ["Customer\nsignup", "Create\norder", "Validate &\nsubmit", "Pay &\nverify",
         "Reserve\nstock", "Activate\nservice", "Send\nSMS", "COMPLETED\n+ timeline"]
x0, y0, w, h, gap = 0.5, 3.2, 1.25, 1.3, 0.28
shapes = []
for i, st in enumerate(steps):
    fill = RED if i == len(steps) - 1 else NAVY
    shp = box(s, Inches(x0 + i * (w + gap)), Inches(y0), Inches(w), Inches(h), fill)
    text(shp, st, size=13, bold=True, color=WHITE, align=PP_ALIGN.CENTER)
    shapes.append(shp)
for i in range(len(shapes) - 1):
    a, b = shapes[i], shapes[i + 1]
    conn = s.shapes.add_connector(MSO_CONNECTOR.STRAIGHT, Inches(0), Inches(0), Inches(0.1), Inches(0.1))
    conn.begin_connect(a, 3)
    conn.end_connect(b, 1)
    conn.line.color.rgb = RED
    conn.line.width = Pt(2.5)
text(box(s, Inches(0.5), Inches(5.2), Inches(12.3), Inches(1.2), WHITE),
     "Any step can fail → automatic compensation (release hold, roll back, park order as FAILED with evidence). "
     "Try: Fulfill end-to-end vs Fulfill (fail at activate).",
     size=16, color=GRAY)

# 6. HOW (stack)
section_slide("HOW — Technology",
              "How is it built?",
              ["Backend: Java 17, Spring Boot 3.2, JPA/Hibernate, WebClient REST, JUnit + Mockito (94 tests).",
               "AI side: Python FastAPI investigator + React console; deterministic evidence, optional LLM reasoning.",
               "Data: PostgreSQL — one database per service; rolling log files with correlation IDs.",
               "Frontend: Angular 22 NexaTel UI (signals, dark/light, carousel) + React investigator.",
               "Contracts: 128 Swagger-documented APIs; Postman-ready curls in chat history.",
               "Roadmap: Kafka events (19) and Docker packaging (20) come last — by design."])

# 7. WHEN / WHERE (run)
section_slide("WHEN & WHERE — Running it",
              "When and where does it run?",
              ["Today: everything on localhost — backends :8081–:8085, UIs :4200/:5173, AI :8090, logs :8899.",
               "Start order: PostgreSQL → 5 jars (mvn spring-boot:run) → npm start (both UIs) → uvicorn + log server.",
               "Health: /actuator/health on every service; Swagger UI on every port; ./scripts/search-logs.sh traces one ID.",
               "Incidents: bash incident-scenarios/run-all.sh rebuilds all 14 broken states in ~2 minutes."])

# 8. USAGES
section_slide("USAGES — Who is it for?",
              "What can you do with it?",
              ["New developers: OVERVIEW → SETUP → running system in ~15 minutes; SUMMARY.md tells the story.",
               "Demo the saga: one click fulfills an order across 4 services — or fails it and shows the rescue.",
               "Practice on-call: 14 Jira-style tickets, each reproducible, each with log + DB evidence.",
               "Watch AI investigate: rule engine + LLM verdicts, RCA documents, fix-type badges, log-source proof.",
               "Portfolio: architecture, testing, docs and a live demo in one private repo."])

# 9. Thank you
title_slide("Thank you",
            "Repo: github.com/Sneha-15-2001/telecom-order-provisioning (private)\n"
            "Start: SUMMARY.md → OVERVIEW.md → SETUP.md → http://localhost:4200")

prs.save("/Users/sneha/telecom-order-provisioning/Telecom-Order-Provisioning-Intro.pptx")
print("saved")
