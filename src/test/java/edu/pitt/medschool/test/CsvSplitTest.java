package edu.pitt.medschool.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CsvSplitTest {

	public static final String DIR = "/tsdb/post-processing/out/";

	public static void main(String[] args) throws IOException {
		split();
	}

	public static void eliminateStartingNAs() throws IOException {
		for (File f : FileUtils.listFiles(new File(DIR + "oo/"), null, true)) {
			FileReader reader = new FileReader(f);
			CSVReader r = new CSVReader(reader);
			boolean end = false;
			int i = 0;
			while (reader.ready() && i <= 100) {
				String[] row = r.readNext();
				i++;
				if (row[3].equals("N/A") || row[4].equals("N/A")) {
					while (reader.ready() && !end) {
						i++;
						String[] endRow = r.readNext();
						if (!endRow[3].equals("N/A") && !endRow[4].equals("N/A")) {
							end = true;
						}
					}
					System.out.println(f.getName() + "," + row[2] + "," + (i - 1));
					break;
				}
			}
			r.close();

			if (end) {
				reader = new FileReader(f);
				r = new CSVReader(reader);
				List<String[]> all = new ArrayList<>();

				int count = 0;
				while (reader.ready()) {
					if (count > i - 2) {
						all.add(r.readNext());
					} else {
						r.readNext();
					}
					count++;
				}
				File newFile = new File(DIR + "kk/" + f.getName());
				Writer writer = new FileWriter(newFile);
				CSVWriter csvWriter = new CSVWriter(writer);

				csvWriter.writeAll(all);
				csvWriter.close();

				System.out.println(f.getName() + ": finished!");
			}

		}
	}

	public static void splitVertical() throws IOException {

		File file = new File(DIR + "long.csv");
		Reader reader = new FileReader(file);
		CSVReader csvReader = new CSVReader(reader);

		Iterator<String[]> iterator = csvReader.iterator();
//        iterator.next();
		int i = 0;

		File newFile = new File(DIR + "long2.csv");
		Writer writer = new FileWriter(newFile);
		CSVWriter csvWriter = new CSVWriter(writer);
		while (iterator.hasNext()) {
			String[] row = iterator.next();

			String[] portion0 = Arrays.copyOfRange(row, 0, 3);
			String[] portion1 = Arrays.copyOfRange(row, 24, row.length);
			
			csvWriter.writeNext(Stream.concat(Stream.of(portion0), Stream.of(portion1)).toArray(String[]::new));
			i++;
			if (i % 1000000 == 0) {
				System.out
						.println("Number of lines in long.csv: " + NumberFormat.getNumberInstance(Locale.US).format(i));
			}
		}
		System.out.println("Number of lines in long.csv: " + NumberFormat.getNumberInstance(Locale.US).format(i));
		csvReader.close();
		csvWriter.close();
	}

	public static void split() throws IOException {

		Map<String, Integer> patientRowMap = new HashMap<>();
		Map<String, Integer> patientLastRowMap = new HashMap<>();

		File file = new File(DIR + "long2.csv");
		Reader reader = new FileReader(file);
		CSVReader csvReader = new CSVReader(reader);

		Iterator<String[]> iterator = csvReader.iterator();
		iterator.next();
		int i = 0;

		Set<String> set = new HashSet<>();

		// String lastId = "";

		while (iterator.hasNext()) {
			String[] row = iterator.next();

			String id = row[0];

			// if (set.contains(id) && !lastId.equals(id)) {
			// System.out.println(id + ": mix");
			// }
			// lastId = id;
			set.add(id);

			patientRowMap.put(id, patientRowMap.getOrDefault(id, 0) + 1);
			patientLastRowMap.put(id, i);
			i++;
			if (i % 1000000 == 0) {
				System.out
						.println("Number of lines in long.csv: " + NumberFormat.getNumberInstance(Locale.US).format(i));
			}
		}

		System.out.println("Number of lines in long.csv: " + NumberFormat.getNumberInstance(Locale.US).format(i));
		csvReader.close();

		Reader areader = new FileReader(file);
		CSVReader newReader = new CSVReader(areader);

		iterator = newReader.iterator();
		iterator.next();

		Map<String, List<String[]>> bigMap = new HashMap<>();
		i = 0;
		while (iterator.hasNext()) {
			String[] row = iterator.next();

			String id = row[0];

			List<String[]> onePatient = bigMap.getOrDefault(id, new ArrayList<>());
			onePatient.add(row);
			bigMap.put(id, onePatient);

			patientRowMap.put(id, patientRowMap.getOrDefault(id, 0) - 1);

			if (patientRowMap.get(id) == 0) {
				File newFile = new File(DIR + "oo2/" + id + ".csv");
				Writer writer = new FileWriter(newFile);
				CSVWriter csvWriter = new CSVWriter(writer);

				csvWriter.writeAll(bigMap.get(id));
				csvWriter.close();

				bigMap.remove(id);
				System.out.println(id + ": finished!");
			}

			i++;
			if (i % 1000000 == 0) {
				System.out
						.println("Number of lines in long.csv: " + NumberFormat.getNumberInstance(Locale.US).format(i));
			}
		}

		System.out.println("Number of lines in long.csv: " + NumberFormat.getNumberInstance(Locale.US).format(i));
		newReader.close();

	}
}
