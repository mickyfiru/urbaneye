package com.urbaneye.app.viewmodels;

import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {
    protected String readableError(Throwable throwable) {
        return throwable == null || throwable.getMessage() == null ? "Ocurrió un error inesperado." : throwable.getMessage();
    }
}
