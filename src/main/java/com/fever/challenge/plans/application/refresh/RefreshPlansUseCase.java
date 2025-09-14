package com.fever.challenge.plans.application.refresh;

import java.time.Duration;

public interface RefreshPlansUseCase {

    /**
     * Dispara la sincronización con el provider en background.
     * Si expira el presupuesto de tiempo, se cancela el trabajo.
     */
    void refreshNonBlocking(Duration timeBudget);

    /**
     * Ejecuta la sincronización de forma síncrona/bloqueante.
     * Pensado para forzar un primer llenado.
     */
    void refreshBlocking(Duration timeout);


}