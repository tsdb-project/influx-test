package edu.pitt.medschool.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import com.opencsv.CSVReader;

import edu.pitt.medschool.config.InfluxappConfig;
import okhttp3.OkHttpClient;

class FileInfo {
<<<<<<< HEAD
	private String name;
	private String uuid;
	private String start;
	private String end;
	private String count;
=======
    private String name;
    private String uuid;
    private String start;
    private String end;
    private String count;
>>>>>>> 9b24cb9e496f3b26a3772053d90daa680de15f5b

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

<<<<<<< HEAD
	public void setEnd(String end) {
		this.end = end;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}
=======
    public void setEnd(String end) {
        this.end = end;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
>>>>>>> 9b24cb9e496f3b26a3772053d90daa680de15f5b
}

public class IntegrityTestFinal {

<<<<<<< HEAD
	static InfluxDB idb = generateIdbClient(true);
	static File integrityFile = new File("/Users/jonathanelmer/Desktop/integrity.csv");
	static File impedenceFile = new File("/Users/jonathanelmer/Desktop/integrity_impedence.csv");
	static FileWriter writer;

	public static void checkPatientFiles(String pid, FileWriter fw) throws IOException {
		String queryTemplate = "SELECT %s(\"I1_1\") FROM \"%s\" GROUP BY *;";

		String queryString = String.format(queryTemplate, "first", pid);

		Query query = new Query(queryString, "data");
		QueryResult result = idb.query(query);
		List<FileInfo> fileInfoList = new ArrayList<>();
=======
    static InfluxDB idb = generateIdbClient(true);
    static File integrityFile = new File("/Users/jonathanelmer/Desktop/integrity.csv");
    static File impedenceFile = new File("/Users/jonathanelmer/Desktop/integrity_impedence.csv");
    static FileWriter writer;
>>>>>>> 9b24cb9e496f3b26a3772053d90daa680de15f5b

		if (result.getResults().get(0).getSeries() != null) {
			for (Result e : result.getResults()) {
				for (Series s : e.getSeries()) {
					for (List<Object> o : s.getValues()) {
						FileInfo info = new FileInfo();
						info.setName(s.getTags().get("fileName"));
						info.setUuid(s.getTags().get("fileUUID"));
						info.setStart(o.get(0).toString());
						fileInfoList.add(info);
					}
				}
			}
		} else {
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
			System.out.println("nothing");
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
			fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
			fw.write("nothing\n");
			fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		}

		queryString = String.format(queryTemplate, "last", pid);
		query = new Query(queryString, "data");
		result = idb.query(query);
		if (result.getResults().get(0).getSeries() != null) {
			for (Result e : result.getResults()) {
				for (Series s : e.getSeries()) {
					for (List<Object> o : s.getValues()) {
						for (int i = 0; i < fileInfoList.size(); i++) {
							if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
									&& fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
								FileInfo info = fileInfoList.get(i);
								info.setEnd(o.get(0).toString());
								fileInfoList.set(i, info);
							}
						}
					}
				}
			}
		}

<<<<<<< HEAD
		queryString = String.format(queryTemplate, "count", pid);
		query = new Query(queryString, "data");
		result = idb.query(query);
		if (result.getResults().get(0).getSeries() != null) {
			for (Result e : result.getResults()) {
				for (Series s : e.getSeries()) {
					for (List<Object> o : s.getValues()) {
						for (int i = 0; i < fileInfoList.size(); i++) {
							if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
									&& fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
								FileInfo info = fileInfoList.get(i);
								info.setCount(Double.valueOf(o.get(1).toString()).intValue() + "");
								fileInfoList.set(i, info);
							}
						}
					}
				}
			}
		}
=======
    public static void checkPatientFiles(String pid, FileWriter fw) throws IOException {
        String queryTemplate = "SELECT %s(\"I1_1\") FROM \"%s\" GROUP BY *;";

        String queryString = String.format(queryTemplate, "first", pid);

        Query query = new Query(queryString, "data");
        QueryResult result = idb.query(query);
        List<FileInfo> fileInfoList = new ArrayList<>();
>>>>>>> 2437d304159342ea9959b2c66230eb6331b1c778

		for (FileInfo fileInfo : fileInfoList) {
			fw.write(fileInfo.getName() + "," + fileInfo.getStart() + "," + fileInfo.getEnd() + ","
					+ fileInfo.getCount() + "," + fileInfo.getUuid() + "\n");
		}
	}

<<<<<<< HEAD
	public static void checkPatientImpedenceFiles() throws IOException {
		FileWriter impedenceWriter = new FileWriter(impedenceFile);
		impedenceWriter.write("time,value,count\n");
		Reader reader = new FileReader(integrityFile);
		CSVReader csvReader = new CSVReader(reader);
		String pid = "";
		while (reader.ready()) {
			String[] fileInfo = csvReader.readNext();
			// if (fileInfo[0].startsWith("PUH-2010-072")) {
			// break;
			// }
			if (!fileInfo[0].startsWith("20")) {
				pid = fileInfo[0];
				impedenceWriter.write("------------\n" + pid + "\n");
			} else {
				impedenceWriter.write(fileInfo[3] + "\n");
				int offset = Integer.parseInt(fileInfo[0].substring(14, 16)) * 60
						+ Integer.parseInt(fileInfo[0].substring(17, 19));
				String queryString = String.format(
						"SELECT mean(\"I192_1\"), count(\"I192_1\") FROM \"%s\" WHERE time >= '%s' AND time <= '%s' AND "
								+ "fileUUID = '%s' AND fileName = '%s' GROUP BY time(1h, %ss)",
						pid, fileInfo[0], fileInfo[1], fileInfo[2], fileInfo[3], offset);
				System.out.println(queryString);
				Query query = new Query(queryString, "data");
				QueryResult result = idb.query(query);
				if (result.getResults().get(0).getSeries() != null) {
					for (Result e : result.getResults()) {
						for (Series s : e.getSeries()) {
							for (List<Object> o : s.getValues()) {
								int i = 0;
								for (Object value : o) {
									i++;
									if (i < 3) {
										impedenceWriter.write(value + ",");
									} else {
										impedenceWriter.write(value + "\n");
									}
								}
							}
						}
					}
				}
			}
		}
		csvReader.close();
		impedenceWriter.close();
		// String queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY
		// time(10m),* LIMIT 12;", pid);
		// Query query = new Query(queryString, "data");
		// QueryResult result = idb.query(query);
		// List<FileInfo> fileInfoList = new ArrayList<>();
		//
		// if (result.getResults().get(0).getSeries() != null) {
		// for (Result e : result.getResults()) {
		// for (Series s : e.getSeries()) {
		// for (List<Object> o : s.getValues()) {
		// FileInfo info = new FileInfo();
		// info.setName(s.getTags().get("fileName"));
		// info.setUuid(s.getTags().get("fileUUID"));
		// info.setStart(o.get(0).toString());
		// fileInfoList.add(info);
		// }
		// }
		// }
		// } else {
		// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
		// System.out.println("nothing");
		// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
		// fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		// fw.write("nothing\n");
		// fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		// }
		//
		// queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY * ORDER
		// BY time DESC LIMIT 1;", pid);
		// query = new Query(queryString, "data");
		// result = idb.query(query);
		// if (result.getResults().get(0).getSeries() != null) {
		// for (Result e : result.getResults()) {
		// for (Series s : e.getSeries()) {
		// for (List<Object> o : s.getValues()) {
		// for (int i = 0; i < fileInfoList.size(); i++) {
		// if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
		// && fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
		// FileInfo info = fileInfoList.get(i);
		// info.setEnd(o.get(0).toString());
		// fileInfoList.set(i, info);
		// }
		// }
		// }
		// }
		// }
		// }
		//
		// for (FileInfo fileInfo : fileInfoList) {
		// fw.write(
		// fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() +
		// "," + fileInfo.getName() + "\n");
		// // System.out.println(
		// // fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() +
		// "," + fileInfo.getName());
		// }
	}

	public static void main(String[] args) throws IOException {
		// checkPatientImpedenceFiles();
=======
        queryString = String.format(queryTemplate, "last", pid);
        query = new Query(queryString, "data");
        result = idb.query(query);
        if (result.getResults().get(0).getSeries() != null) {
            for (Result e : result.getResults()) {
                for (Series s : e.getSeries()) {
                    for (List<Object> o : s.getValues()) {
                        for (int i = 0; i < fileInfoList.size(); i++) {
                            if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
                                    && fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
                                FileInfo info = fileInfoList.get(i);
                                info.setEnd(o.get(0).toString());
                                fileInfoList.set(i, info);
                            }
                        }
                    }
                }
            }
        }

        queryString = String.format(queryTemplate, "count", pid);
        query = new Query(queryString, "data");
        result = idb.query(query);
        if (result.getResults().get(0).getSeries() != null) {
            for (Result e : result.getResults()) {
                for (Series s : e.getSeries()) {
                    for (List<Object> o : s.getValues()) {
                        for (int i = 0; i < fileInfoList.size(); i++) {
                            if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
                                    && fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
                                FileInfo info = fileInfoList.get(i);
                                info.setCount(Double.valueOf(o.get(1).toString()).intValue() + "");
                                fileInfoList.set(i, info);
                            }
                        }
                    }
                }
            }
        }

        for (FileInfo fileInfo : fileInfoList) {
            fw.write(fileInfo.getName() + "," + fileInfo.getStart() + "," + fileInfo.getEnd() + ","
                    + fileInfo.getCount() + "," + fileInfo.getUuid() + "\n");
        }
    }
>>>>>>> 2437d304159342ea9959b2c66230eb6331b1c778

<<<<<<< HEAD
		writer = new FileWriter(integrityFile);
=======
    public static void checkPatientImpedenceFiles() throws IOException {
        FileWriter impedenceWriter = new FileWriter(impedenceFile);
        impedenceWriter.write("time,value,count\n");
        Reader reader = new FileReader(integrityFile);
        CSVReader csvReader = new CSVReader(reader);
        String pid = "";
        while (reader.ready()) {
            String[] fileInfo = csvReader.readNext();
            // if (fileInfo[0].startsWith("PUH-2010-072")) {
            // break;
            // }
            if (!fileInfo[0].startsWith("20")) {
                pid = fileInfo[0];
                impedenceWriter.write("------------\n" + pid + "\n");
            } else {
                impedenceWriter.write(fileInfo[3] + "\n");
                int offset = Integer.parseInt(fileInfo[0].substring(14, 16)) * 60
                        + Integer.parseInt(fileInfo[0].substring(17, 19));
                String queryString = String.format(
                        "SELECT mean(\"I192_1\"), count(\"I192_1\") FROM \"%s\" WHERE time >= '%s' AND time <= '%s' AND "
                                + "fileUUID = '%s' AND fileName = '%s' GROUP BY time(1h, %ss)",
                        pid, fileInfo[0], fileInfo[1], fileInfo[2], fileInfo[3], offset);
                System.out.println(queryString);
                Query query = new Query(queryString, "data");
                QueryResult result = idb.query(query);
                if (result.getResults().get(0).getSeries() != null) {
                    for (Result e : result.getResults()) {
                        for (Series s : e.getSeries()) {
                            for (List<Object> o : s.getValues()) {
                                int i = 0;
                                for (Object value : o) {
                                    i++;
                                    if (i < 3) {
                                        impedenceWriter.write(value + ",");
                                    } else {
                                        impedenceWriter.write(value + "\n");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        csvReader.close();
        impedenceWriter.close();
        // String queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY
        // time(10m),* LIMIT 12;", pid);
        // Query query = new Query(queryString, "data");
        // QueryResult result = idb.query(query);
        // List<FileInfo> fileInfoList = new ArrayList<>();
        //
        // if (result.getResults().get(0).getSeries() != null) {
        // for (Result e : result.getResults()) {
        // for (Series s : e.getSeries()) {
        // for (List<Object> o : s.getValues()) {
        // FileInfo info = new FileInfo();
        // info.setName(s.getTags().get("fileName"));
        // info.setUuid(s.getTags().get("fileUUID"));
        // info.setStart(o.get(0).toString());
        // fileInfoList.add(info);
        // }
        // }
        // }
        // } else {
        // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
        // System.out.println("nothing");
        // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
        // fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        // fw.write("nothing\n");
        // fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        // }
        //
        // queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY * ORDER
        // BY time DESC LIMIT 1;", pid);
        // query = new Query(queryString, "data");
        // result = idb.query(query);
        // if (result.getResults().get(0).getSeries() != null) {
        // for (Result e : result.getResults()) {
        // for (Series s : e.getSeries()) {
        // for (List<Object> o : s.getValues()) {
        // for (int i = 0; i < fileInfoList.size(); i++) {
        // if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
        // && fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
        // FileInfo info = fileInfoList.get(i);
        // info.setEnd(o.get(0).toString());
        // fileInfoList.set(i, info);
        // }
        // }
        // }
        // }
        // }
        // }
        //
        // for (FileInfo fileInfo : fileInfoList) {
        // fw.write(
        // fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() +
        // "," + fileInfo.getName() + "\n");
        // // System.out.println(
        // // fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() +
        // "," + fileInfo.getName());
        // }
    }
>>>>>>> 9b24cb9e496f3b26a3772053d90daa680de15f5b

<<<<<<< HEAD
		String[] pidsArr = new String[] { "PUH-2015-198" };
		for (String pid : pidsArr) {
			System.out.println(pid);
			writer.write(pid + "\n");
			checkPatientFiles(pid, writer);
		}
		writer.close();
	}
=======
    public static void main(String[] args) throws IOException {
        // checkPatientImpedenceFiles();

        writer = new FileWriter(integrityFile);

        String[] pidsArr = new String[] { "PUH-2015-198" };
        for (String pid : pidsArr) {
            System.out.println(pid);
            writer.write(pid + "\n");
            checkPatientFiles(pid, writer);
        }
        writer.close();
    }
>>>>>>> 2437d304159342ea9959b2c66230eb6331b1c778

	private static InfluxDB generateIdbClient(boolean needGzip) {
		InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
				InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
						.readTimeout(90, TimeUnit.MINUTES).writeTimeout(120, TimeUnit.SECONDS));
		if (needGzip) {
			idb.enableGzip();
		} else {
			idb.disableGzip();
		}
		return idb;
	}

}
