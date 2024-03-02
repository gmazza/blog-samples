package net.glenmazza.domoclient.model;

import java.util.List;

public class DataSetSchema {

    List<DataSetColumn> columns;

    public List<DataSetColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DataSetColumn> columns) {
        this.columns = columns;
    }
}
