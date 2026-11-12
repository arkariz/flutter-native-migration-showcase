package com.example.legacyshell.flow

/**
 * Marks a crossing point that exists only *during* the migration and must be deleted
 * after it (docs/adr/0010). [removeWhen] is the death clause: the concrete,
 * checkable condition under which this code goes away. `NativeRoutesTest` fails if
 * it's blank, and `grep -r @TemporaryCrossing` lists every such debt.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TemporaryCrossing(val removeWhen: String)
