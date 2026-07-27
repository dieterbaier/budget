Feature: Maintain category groups and categories
  As the owner I want to organise my spending into the groups and categories I
  actually think in, and to correct them later without losing history.

  Scenario: Organise categories into groups
    Given the category group "House"
    And the category group "Car"
    When I add the category "Groceries" to "House"
    And I add the category "Fuel" to "Car"
    Then the categories are:
      | name      | group |
      | Fuel      | Car   |
      | Groceries | House |

  Scenario: Renaming a category keeps the transactions that use it
    Given the category group "House"
    And the category "Grocries" in "House"
    And a transaction of 42.00 EUR in category "Grocries"
    When I rename the category "Grocries" to "Groceries"
    Then the category "Groceries" exists
    And "Groceries" has 1 transaction

  Scenario: Renaming a group keeps its categories
    Given the category group "Huose"
    And the category "Groceries" in "Huose"
    When I rename the category group "Huose" to "House"
    Then the category "Groceries" is in group "House"

  Scenario: A name is only free once
    Given the category group "House"
    And the category "Groceries" in "House"
    When I add the category "Groceries" to "House"
    Then it is rejected because the name is taken

  Scenario: A category in use cannot be deleted
    Given the category group "House"
    And the category "Groceries" in "House"
    And a transaction of 42.00 EUR in category "Groceries"
    When I delete the category "Groceries"
    Then it is rejected because it is still in use
    And the category "Groceries" exists

  Scenario: An unused category can be deleted
    Given the category group "House"
    And the category "Mistake" in "House"
    When I delete the category "Mistake"
    Then the category "Mistake" is gone
