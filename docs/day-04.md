# Day 4 — User Roles & Permissions Access Foundation

We have successfully designed and implemented the relational user-role database model and the dynamic, role-based dashboard simulation on the frontend.

---

## 1. Accomplishments
* **Built Backend Relational RBAC Schema**:
  * Connected H2 in-memory database and configured JPA settings.
  * Created JPA entities: `Tenant`, `User` (`app_user` mapping), `Role`, and `Permission`.
  * Configured relationship mappings (Many-to-Many tables `user_role` and `role_permission`).
  * Implemented authentication services (`RoleService`, `PermissionService`, `UserRoleService`).
  * Created REST endpoints in `AuthController` to retrieve roles, permissions, and user permissions mappings.
  * Seeded initial tenant and six distinct employee and customer accounts on startup in `DataInitializer`.
* **Built Interactive Frontend Role Views**:
  * Created `RoleStateService` to manage simulated role changes and invoke permissions checks.
  * Added the dynamic **Demo role switcher** dropdown selection inside the dashboard top bar.
  * Bound sidebar menu navigation items to conditional permission states.
  * Segmented `OverviewComponent` to render unique dashboard layouts, metrics, and actions tailored specifically to the active role (Owner, Admin, Sales, Warehouse, Driver, Customer).
  * Built the `/user-roles` page displaying tabbed details of each role's primary responsibility, allow/deny summaries, active database permissions, and a responsive permissions matrix table.
* **Wrote Security Documentation**:
  * Outlined boundaries in `docs/security.md` and explained implementation parameters in `docs/user-roles.md`.

---

## 2. Code Files Created/Modified

### Backend
* [pom.xml](file:///c:/dev/rentflow-ai/backend/pom.xml): Added JPA and H2.
* [application.properties](file:///c:/dev/rentflow-ai/backend/src/main/resources/application.properties): Set H2 console.
* [Tenant.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/tenant/Tenant.java): Tenant Entity.
* [TenantRepository.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/tenant/TenantRepository.java): Tenant Database operations.
* [PermissionCode.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/permission/PermissionCode.java): Permissions Enums.
* [Permission.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/permission/Permission.java): Permission Entity.
* [PermissionRepository.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/permission/PermissionRepository.java): Permission DB operations.
* [RoleType.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/role/RoleType.java): Roles Enums.
* [Role.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/role/Role.java): Role Entity.
* [RoleRepository.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/role/RoleRepository.java): Role DB operations.
* [User.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/user/User.java): User Entity.
* [UserRepository.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/user/UserRepository.java): User DB operations.
* [RoleService.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/auth/RoleService.java): Role listings.
* [PermissionService.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/auth/PermissionService.java): Permission listings.
* [UserRoleService.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/auth/UserRoleService.java): Assignment validations.
* [AuthController.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/auth/AuthController.java): Rest Endpoints.
* [DataInitializer.java](file:///c:/dev/rentflow-ai/backend/src/main/java/com/rentflow/auth/DataInitializer.java): Seed loader.

### Frontend
* [role-state.service.ts](file:///c:/dev/rentflow-ai/frontend/src/app/services/role-state.service.ts): Dynamic switcher service.
* [user-roles.component.ts](file:///c:/dev/rentflow-ai/frontend/src/app/user-roles/user-roles.component.ts): Role detail controller.
* [user-roles.component.html](file:///c:/dev/rentflow-ai/frontend/src/app/user-roles/user-roles.component.html): Template design.
* [user-roles.component.scss](file:///c:/dev/rentflow-ai/frontend/src/app/user-roles/user-roles.component.scss): Style details.
* [user-roles.component.spec.ts](file:///c:/dev/rentflow-ai/frontend/src/app/user-roles/user-roles.component.spec.ts): Specs assertions.
* [app.routes.ts](file:///c:/dev/rentflow-ai/frontend/src/app/app.routes.ts): Route registry.
* [dashboard.component.ts](file:///c:/dev/rentflow-ai/frontend/src/app/dashboard/dashboard.component.ts): Swapper bindings.
* [dashboard.component.html](file:///c:/dev/rentflow-ai/frontend/src/app/dashboard/dashboard.component.html): Switcher template.
* [dashboard.component.scss](file:///c:/dev/rentflow-ai/frontend/src/app/dashboard/dashboard.component.scss): Switcher styling.
* [overview.component.ts](file:///c:/dev/rentflow-ai/frontend/src/app/dashboard/overview/overview.component.ts): Active role bindings.
* [overview.component.html](file:///c:/dev/rentflow-ai/frontend/src/app/dashboard/overview/overview.component.html): Dynamic segmentation.
* [overview.component.scss](file:///c:/dev/rentflow-ai/frontend/src/app/dashboard/overview/overview.component.scss): Segment stylings.

---

## 3. Running & Verifying
* Start Backend: `mvn spring-boot:run` in `/backend` (Runs on Port 8080).
* Start Frontend: `npm start` in `/frontend` (Runs on Port 4200).
* API verification: `GET http://localhost:8080/api/roles` shows the mapped JSON objects.
