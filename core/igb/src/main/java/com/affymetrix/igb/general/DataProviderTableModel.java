package com.affymetrix.igb.general;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.genometry.data.DataProvider;
import com.affymetrix.genometry.data.DataProviderComparator;
import com.affymetrix.genometry.util.LoadUtils.ResourceStatus;
import com.affymetrix.genometry.util.ModalUtils;
import com.affymetrix.genometry.util.PreferenceUtils;
import com.affymetrix.igb.EventService;
import com.affymetrix.igb.general.DataProviderManager.DataProviderServiceChangeEvent;
import static com.affymetrix.igb.general.DataProviderTableModel.DataProviderTableColumn.Enabled;
import static com.affymetrix.igb.general.DataProviderTableModel.DataProviderTableColumn.Name;
import static com.affymetrix.igb.general.DataProviderTableModel.DataProviderTableColumn.Refresh;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dcnorris
 */
@Component(name = DataProviderTableModel.COMPONENT_NAME, immediate = true, provide = DataProviderTableModel.class)
public final class DataProviderTableModel extends AbstractTableModel {

    public static final String COMPONENT_NAME = "DataProviderTableModel";
    private DataProviderManager dataProviderManager;
    private EventService eventService;
    private EventBus eventBus;

    public static enum DataProviderTableColumn {

        Refresh, Name, Type, URL, Enabled
    }

    private final List<DataProviderTableColumn> tableColumns;
    private List<DataProvider> sortedDataProviders;

    public DataProviderTableModel() {
        tableColumns = Lists.newArrayList(DataProviderTableColumn.values());
        sortDataSources();
    }

    @Activate
    public void activate() {
        eventBus = eventService.getEventBus();
        eventBus.register(this);
    }

    @Reference
    public void setDataProviderManager(DataProviderManager dataProviderManager) {
        this.dataProviderManager = dataProviderManager;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Subscribe
    public void refreshEvent(DataProviderServiceChangeEvent event) {
        sortDataSources();
    }

    public void sortDataSources() {
        sortedDataProviders = Lists.newArrayList(DataProviderManager.getAllServers());
        Collections.sort(sortedDataProviders, new DataProviderComparator());
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    int getColumnIndex(DataProviderTableColumn dataProviderTableColumn) {
        return tableColumns.indexOf(dataProviderTableColumn);
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Refresh";
        } else if (column == 1) {
            return "Name";
        } else if (column == 2) {
            return "Type";
        } else if (column == 3) {
            return "URL";
        } else if (column == 4) {
            return "Enabled";
        }

        throw new IllegalStateException();
    }

    DataProvider getElementAt(int row) {
        return sortedDataProviders.get(row);
    }

    int getRowFromDataProvider(DataProvider dataProvider) {
        return sortedDataProviders.indexOf(dataProvider);
    }

    public Object getColumnValue(DataProvider dataProvider, int columnIndex) {
        if (columnIndex >= getColumnCount()) {
            throw new IllegalStateException();
        }
        switch (tableColumns.get(columnIndex)) {
            case Refresh:
                return "";
            case Name:
                return dataProvider.getName();
            case Type:
                return dataProvider.getFactoryName().get();
            case URL:
                return dataProvider.getUrl();
            case Enabled:
                return dataProvider.getStatus() != ResourceStatus.Disabled;
            default:
                throw new IllegalArgumentException("columnIndex " + columnIndex + " is out of range");
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex >= getColumnCount()) {
            throw new IllegalStateException();
        }
        switch (tableColumns.get(columnIndex)) {
            case Refresh: {
                return ImageIcon.class;
            }
            case Enabled: {
                return Boolean.class;
            }
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        DataProvider dataProvider = sortedDataProviders.get(rowIndex);
        return isEditable(dataProvider, columnIndex);
    }

    public boolean isEditable(DataProvider dataProvider, int columnIndex) {
        switch (tableColumns.get(columnIndex)) {
            case Refresh: {
                if (dataProvider.getStatus() != ResourceStatus.Disabled) {
                    return true;
                }
            }
            case Name: {
                if (dataProvider.getStatus() != ResourceStatus.Disabled) {
                    return true;
                }
            }
            case Enabled: {
                return true;
            }
            default:
                return false;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        DataProvider dataProvider = sortedDataProviders.get(rowIndex);
        setColumnValue(dataProvider, aValue, columnIndex);
    }

    public void setColumnValue(DataProvider dataProvider, Object editedValue, int column) {
        switch (tableColumns.get(column)) {
            case Refresh:
                if ((Boolean) getValueAt(column, getColumnIndex(DataProviderTableColumn.Enabled))) {
                    if (dataProvider.getStatus() == ResourceStatus.Disabled
                            && confirmRefresh()) {
                        dataProvider.setStatus(ResourceStatus.NotInitialized);
                    }
                }
                fireTableRowsUpdated(sortedDataProviders.indexOf(dataProvider), sortedDataProviders.indexOf(dataProvider));
                break;
            case Enabled:
                if ((Boolean) editedValue) {
                    dataProvider.setStatus(ResourceStatus.NotInitialized);
                } else {
                    if (confirmDelete()) {
                        dataProvider.setStatus(ResourceStatus.Disabled);
                    }
                }
                fireTableRowsUpdated(sortedDataProviders.indexOf(dataProvider), sortedDataProviders.indexOf(dataProvider));
                break;
            case Name:
                dataProvider.setName((String) editedValue);
                break;
            case URL:
                //do nothing
                break;
            case Type:
                //do nothing
                break;
            default: {
                throw new IllegalArgumentException("columnIndex " + column + " not editable");
            }

        }

    }

    @Override
    public int getRowCount() {
        return sortedDataProviders.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataProvider dataProvider = sortedDataProviders.get(rowIndex);
        return getColumnValue(dataProvider, columnIndex);
    }

    public boolean confirmRefresh() {
        String message = "Warning:\n"
                + "Refreshing the server will force IGB to re-read configuration files from the server.\n"
                + "This means all data sets currently loaded from the server will be deleted.\n"
                + "This is useful mainly for setting up or configuring a QuickLoad site.";

        return ModalUtils.confirmPanel(
                message, PreferenceUtils.getTopNode(),
                PreferenceUtils.CONFIRM_BEFORE_REFRESH,
                PreferenceUtils.default_confirm_before_refresh);
    }

    public boolean confirmDelete() {
        String message = "Warning:\n"
                + "Disabling or removing a server will cause any"
                + " currently loaded data from that server to be removed from IGB.\n";

        return ModalUtils.confirmPanel(
                message, PreferenceUtils.getTopNode(),
                PreferenceUtils.CONFIRM_BEFORE_DELETE,
                PreferenceUtils.default_confirm_before_delete);
    }
}
