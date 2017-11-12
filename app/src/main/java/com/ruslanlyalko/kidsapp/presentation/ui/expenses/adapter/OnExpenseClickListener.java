package com.ruslanlyalko.kidsapp.presentation.ui.expenses.adapter;

import com.ruslanlyalko.kidsapp.data.models.Expense;

/**
 * Created by Ruslan Lyalko
 * on 12.11.2017.
 */

public interface OnExpenseClickListener {

    void onRemoveClicked(Expense expense);

    void onPhotoPreviewClicked(Expense expense);
}
