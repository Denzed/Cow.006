package com.cow006.gui.tableactivities;

import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.cow006.gui.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class Helper {
    static void fillTable(GridView table, List<List<String>> data) {
        if (data.isEmpty()) {
            return;
        }
        table.setNumColumns(data.get(0).size());
        System.out.println("HELPER: " + data.size());
        List<String> joinedData = new LinkedList<>();
        for (List<String> row : data) {
            joinedData.addAll(row);
//            System.out.println("HELPER: added " + row.get(0) + "\t" + row.get(1) + "\t\ttmp: " + joinedData);
        }
        final ArrayAdapter<String> gridViewArrayAdapter =
                new ArrayAdapter<>(table.getContext(),
                        R.layout.grid_view_text,
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
