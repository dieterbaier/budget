Feature: Current monthly expenditure
  As the owner I want to see my current monthly expenditure and whether I am
  overspending compared to my average monthly income.

  Scenario: Variable costs plus amortized fixed costs signal overspending
    Given the average monthly income is 950.00 EUR
    And a yearly fixed cost "Car insurance" of 1200.00 EUR
    And in month 2026-07 the following expenses:
      | category  | amount |
      | Groceries | 800.00 |
      | Fuel      | 150.00 |
    And in month 2026-07 a refund of 50.00 EUR in category "Groceries"
    And in month 2026-07 a transfer of 500.00 EUR
    When I calculate the monthly expenditure for 2026-07
    Then the fixed costs share is 100.00 EUR
    And the variable costs are 900.00 EUR
    And the total expenditure is 1000.00 EUR
    And it is overspending
