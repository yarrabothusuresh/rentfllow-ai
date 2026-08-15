# Date-Based Availability Calculation Engine

## Overview
Unlike standard e-commerce software where inventory availability is static, event rental assets are reused across multiple dates. RentFlow AI calculates real-time inventory availability dynamically over any requested date/time window $[t_{\text{start}}, t_{\text{end}}]$.

## Date Overlap Condition Rule
Two date intervals $[S_1, E_1]$ and $[S_2, E_2]$ overlap if and only if:
$$\text{Overlap} \iff S_1 < E_2 \land E_1 > S_2$$

### Exact Boundary Rule
If an existing reservation ends at $12:00\text{ PM}$ ($E_1 = 12:00$) and a new requested reservation starts at $12:00\text{ PM}$ ($S_2 = 12:00$), $E_1 \le S_2$ holds.
Therefore, boundary turnarounds are **NON-overlapping**, allowing seamless return-to-checkout transitions.

## Dynamic Availability Formula
For any product $P$ over requested period $[t_{\text{start}}, t_{\text{end}}]$:

$$\text{availableQuantity} = \text{quantityOwned} - \text{quantityInMaintenance} - \text{quantityDamaged} - \text{quantityLost} - \sum_{\text{overlapping } R} R.\text{quantity}$$

Where overlapping $R$ are active `InventoryReservation` records in state `RESERVED` or `PENDING` satisfying $R.\text{startDateTime} < t_{\text{end}} \land R.\text{endDateTime} > t_{\text{start}}$.

## Shortage Computation
$$\text{available} = (\text{availableQuantity} \ge \text{requestedQuantity})$$
$$\text{shortageQuantity} = \begin{cases} 0 & \text{if available} \\ \text{requestedQuantity} - \text{availableQuantity} & \text{if shortage} \end{cases}$$
