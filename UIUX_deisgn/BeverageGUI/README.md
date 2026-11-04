# Beverage Management System – GUI tier (JavaFX)

Runs with fake data, no database needed.

## Run (Maven)
    mvn javafx:run

## Run in IntelliJ / Eclipse
1. Import as a Maven project (JDK 17+).
2. Run `gui.MainApp`.

## Test accounts (same as the report)
- manager01 / 123456  → sees everything
- staff01   / 123456  → no manager-only menu

## Connect the real backend (3-tier model)
1. Open `gui/service/AppService.java`. This is the contract the GUI uses.
2. Ask the controller teammate to implement it (or write an adapter that calls their classes).
3. In `MainApp.java`, change `new MockAppService()` to your implementation.
The GUI never touches the database.

## What each role can do
- Staff: take orders, change table status (right-click a table), view orders, search the menu.
- Manager: everything above plus add/edit/delete products, categories, employees and tables.

## Screens
Login, Tables, Order, Orders list, Menu (read-only), and for Managers: Products, Categories, Employees, Manage Tables.
Every screen follows the same pattern (toolbar + table + dialogs), so new ones are easy to copy from `CategoriesView.java`.
