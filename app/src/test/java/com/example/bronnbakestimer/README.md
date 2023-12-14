# BronnBakesTimer Unit Tests

This `README.md` is located in the `app/src/test/java/com/example/bronnbakestimer` directory of the BronnBakesTimer project. It provides an overview of the unit test structure and organization, guiding developers on where to find and place new tests.

## Overview of Test Directory

The unit tests are organized to mirror the structure of the main source code, ensuring a clear and logical association between test classes and the corresponding source files they test.

## Test Package Structure

### ./di/
Contains tests for Dependency Injection modules, like `TestDiModule.kt`.

### ./logic/
Houses tests for business logic classes, such as `DefaultInputValidatorTest.kt`, `UserInputTimeUnitTypeTest.kt`, and `ValidationResultTest.kt`.

### ./model/
Includes tests for data models, e.g., `ExtraTimerInputsDataTest.kt`.

### ./provider/
Tests for service providers and wrappers, such as `CoroutineScopeProviderWrapperTest.kt`, `ProductionCoroutineScopeProviderTest.kt`, and `TestCoroutineScopeProvider.kt`.

### ./repository/
Ensures the proper functioning of data repositories, including `DefaultExtraTimersCountdownRepositoryTest.kt` and `DefaultTimerRepositoryTest.kt`.

### ./service/
Contains tests related to services, including `CountdownLogicTest.kt`.

### ./ui/
Tests for User Interface components and logic. *Currently, no tests are present in this category.*

### ./util/
Contains tests for utility classes and functions, such as `TimerUserInputDataIdTest.kt` and `UtilsKtTest.kt`.

### ./viewmodel/
Tests for ViewModel classes, including `BronnBakesTimerViewModelTest.kt`.

## Guidelines for Adding New Tests

- **Consistent Naming**: Test class names should reflect the classes they test, typically appending `Test`.
- **Test Placement**: Place new tests in the appropriate package that mirrors the main source code.
- **Documentation**: Update this `README.md` for significant changes or new test types.

## Running Tests

- Tests can be run within the IDE or via command-line Gradle commands.
- Ensure tests are part of your CI pipeline for continuous integration.

---

*Note: This document is current as of its last update. Future changes to the test structure should be reflected here.*

