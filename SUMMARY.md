# The Story of One Mobile Connection — told through our system

Meet **Priya**. Her old phone plan expired, and she wants a new 5G connection
with a fresh number. This is what happens — step by step — inside the
Telecom Order Provisioning System you now have on GitHub.

## Chapter 1 — "I want a new connection"

Priya walks into the store (in our world: someone opens the NexaTel website
and clicks **New customer**). The shop agent types her name, email, and phone.
Behind the screen, `customer-service` writes her a customer number —
`CUS-…` — and marks her ACTIVE. She officially exists now.

*What you'd see: a new row in Customers, status green.*

## Chapter 2 — "Give me the 5G plan"

Priya picks the ₹299 5G plan. The agent clicks **New order**, and
`order-service` opens order `ORD-…` in state CREATED — a shopping cart with
one line in it. Nothing is promised yet; it's just a cart.

## Chapter 3 — "Is she allowed?"

Before anything moves, the order asks customer-service: *is Priya eligible?*
She is ACTIVE, so validation passes and the order turns VALIDATED. Had her
account been suspended, the order would have died right here — politely, with
the reason written down. (That exact heartbreak is our incident INC-10106.)

## Chapter 4 — "Pay first, please"

Priya pays ₹299 over UPI. The system records the attempt — then double-checks
it against the bill, like a cashier counting change. Amount matches? Order
moves to PAYMENT_COMPLETED. If she'd underpaid by even a rupee, the order
would flip to FAILED and wait for a retry. (INC-10101 is the story of a
payment that was recorded but never checked — money taken, order frozen.)

## Chapter 5 — "Reserve her number"

Now the system must set aside a *real* mobile number for Priya — one nobody
else can take. `inventory-service` scans the pool of free numbers, picks one,
and puts a hold on it for her order: RESERVED. Think of a librarian pulling a
book off the shelf and writing her name on the slip. (INC-10103 is what happens
when that slip is never collected — the number sits reserved forever.)

## Chapter 6 — "Switch it on"

With the number reserved, `provisioning-service` performs the "activation" —
in the real world this lights up towers and SIM profiles; in ours it's a
faithful simulation with the same rules. Number present? Plan valid? Then
COMPLETED. Missing number? FAILED with the reason stamped on it. (INC-10102:
an eSIM order arrived with no number at all — classic batch mistake.)

## Chapter 7 — "Tell her the good news"

Finally, `notification-service` composes an SMS from a template —
*"Hi, your order ORD-… is complete"* — and sends it to Priya's phone.
She walks out connected. The order shows COMPLETED, and its **timeline**
lists every chapter above with timestamps.

## The button that runs chapters 5–7 at once

On the order page there's a **Fulfill end-to-end** button. One click runs
reserve → provision → complete → notify as a *saga*: if activation fails
halfway, the system automatically releases the number, rolls provisioning
back, and parks the order as FAILED *with the evidence attached* — no silent
mess, no half-done state. Pressing **Fulfill (fail at activate)** shows this
rescue live.

## When the story breaks (our 10 incident tickets)

Real systems fail mid-story, and ours rehearses it on purpose. Each ticket in
`incident-scenarios/` is a short tragedy you can replay: the duplicate SMS
sent twice (INC-10105), the discount that vanished after an edit (INC-10109),
the SIM hold that expired but never freed its number (INC-10110), the order
list that slows down as data grows (INC-10108)… Every one is reproducible with
a single script, and every one is waiting for the project's next chapters —
an AI investigator that reads these stories in the logs and names the culprit.

## The one-line version

**A customer orders a connection; the system checks her, takes her money,
reserves her number, switches it on, texts her — and cleans up after itself
when anything breaks.** Everything else in this repository exists to make that
sentence true, visible, and demoable.
