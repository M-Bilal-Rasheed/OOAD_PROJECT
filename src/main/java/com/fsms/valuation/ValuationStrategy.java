package com.fsms.valuation;

import com.fsms.model.Player;

// ============================================================
// Abstraction: ValuationStrategy is an INTERFACE.
// An interface defines WHAT to do, not HOW to do it.
// Any class that implements this interface must provide
// its own implementation of calculateValue().
//
// Strategy Design Pattern: This interface allows different
// valuation methods to be swapped in without changing the
// code that calls it (e.g., PlayerService).
// ============================================================
public interface ValuationStrategy {  // Abstraction + Strategy Design Pattern

    // This method must be implemented by any class using this interface.
    // It takes a Player and returns an estimated value in pounds (£).
    long calculateValue(Player player);
}
