package xspread.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class CSV extends File {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8030109493652994118L;
	private ArrayList<String> fields = new ArrayList<String>();
	private int size;

	public CSV() {
		super("");
	}

	public CSV(String fileName) {
		super(fileName);
		try {
			this.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void read() throws FileNotFoundException, IOException {

		try (BufferedReader br = new BufferedReader(new FileReader(this))) {
			String ln = br.readLine();
			if (ln == null) {
				throw new IOException(this.getName() + " is empty.");
			}
			StringTokenizer stk = new StringTokenizer(ln, ",");
			while (stk.hasMoreTokens()) {
				fields.add(stk.nextToken().toLowerCase());
			}
			int ct = 0;
			ln = br.readLine();
			while (ln != null) {
				ct++;
				ln = br.readLine();
			}
			size = ct;
		}
	}

	public Number[] getColumn(String name) throws FileNotFoundException,
			IOException, ParseException {

		Number[] column = new Number[size];

		try (BufferedReader br = new BufferedReader(new FileReader(this))) {
			String ln = br.readLine();
			if (ln == null) {
				throw new IOException(this.getName() + " is empty.");
			}
			ln = br.readLine();
			int idx = fields.indexOf(name.toLowerCase());
			if (idx == -1) {
				throw new IOException("Field name \"" + name
						+ "\" was not found in " + this.getPath() + ".");
			}
			int ct = 0;
			while (ln != null) {
				String[] split = ln.split(",");
				column[ct] = NumberFormat.getInstance().parse(split[idx]);

				ln = br.readLine();
				ct++;
			}
		}
		return column;
	}

	public static void printColumn(Number[] column) {
		System.out.println(Arrays.toString(column));
	}

	public TableView<ObservableList<StringProperty>> tableView(
			final boolean hasHeader) {
		TableView<ObservableList<StringProperty>> table = new TableView<>();
		table.setPlaceholder(new Label("Loading..."));
		File file = this;
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				BufferedReader in = new BufferedReader(new FileReader(file));
				// Header line
				if (hasHeader) {
					final String headerLine = in.readLine();
					final String[] headerValues = headerLine.split(",");
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							for (int column = 0; column < headerValues.length; column++) {
								table.getColumns().add(
										createTableColumn(column,
												headerValues[column]));
							}
						}
					});
				}

				// Data:

				String dataLine;
				while ((dataLine = in.readLine()) != null) {
					final String[] dataValues = dataLine.split(",");
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							// Add additional columns if necessary:
							for (int columnIndex = table.getColumns().size(); columnIndex < dataValues.length; columnIndex++) {
								table.getColumns().add(
										createTableColumn(columnIndex, ""));
							}
							// Add data to table:
							ObservableList<StringProperty> data = FXCollections
									.observableArrayList();
							for (String value : dataValues) {
								data.add(new SimpleStringProperty(value));
							}
							table.getItems().add(data);
						}
					});
				}

				in.close();
				return null;
			}
		};
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
		return table;
	}

	private TableColumn<ObservableList<StringProperty>, String> createTableColumn(
			final int columnIndex, String columnTitle) {
		TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
		String title;
		if (columnTitle == null || columnTitle.trim().length() == 0) {
			title = "Column " + (columnIndex + 1);
		} else {
			title = columnTitle;
		}
		column.setText(title);
		column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<StringProperty>, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(
					CellDataFeatures<ObservableList<StringProperty>, String> cellDataFeatures) {
				ObservableList<StringProperty> values = cellDataFeatures
						.getValue();
				if (columnIndex >= values.size()) {
					return new SimpleStringProperty("");
				} else {
					return cellDataFeatures.getValue().get(columnIndex);
				}
			}
		});
		return column;
	}
}
