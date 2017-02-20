package com.cow006.gui.tableactivities;

import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Helper {
    static void fillTable(GridView table, List<List<String>> data) {
        table.setNumColumns(data.get(0).size());
        List<String> joinedData = new LinkedList<>();
        for (List<String> row : data) {
            joinedData.addAll(row);
        }
        final ArrayAdapter<String> gridViewArrayAdapter =
                new ArrayAdapter<>(table.getContext(),
                        android.R.layout.simple_list_item_1,
                        joinedData);
        table.setAdapter(gridViewArrayAdapter);
    }

    static List<List<String>> parseStringToTable(String serialisedTable) {
        List<List<String>> scores = new LinkedList<>();
        for (String lineString : serialisedTable.split("\n\n")) {
            scores.add(Arrays.asList(lineString.split("\n")));
        }
        return scores;
    }
}
