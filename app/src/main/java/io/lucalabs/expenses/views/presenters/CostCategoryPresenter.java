package io.lucalabs.expenses.views.presenters;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.CostCategory;

public class CostCategoryPresenter {
    public static List<String> selectOptions(Context context, List<CostCategory> costCategories, boolean addEmptyOption){
        List<String> categoryNames = new ArrayList<>();

        if(addEmptyOption)
            categoryNames.add(context.getString(R.string.no_category));

        for(CostCategory category : costCategories)
            categoryNames.add(category.getLocal_name());

        return categoryNames;
    }
}
