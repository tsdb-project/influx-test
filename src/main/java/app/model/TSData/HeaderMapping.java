package app.model.TSData;

/**
 * Forward mapping on headers
 */
public class HeaderMapping {
    private long id;
    private String electrode, sid, type, notes;
    private int count;
    private double freq_low, freq_high;

    /**
     * New `HeaderMapping` object
     * @param id        Which column (2-6038)
     * @param t         Header type ('FFT Spectrogram')
     * @param electrode Electrode info ('P4-O2')
     * @param freq_low  Frequency low bound
     * @param freq_high Frequency high bound
     * @param notes     Special notes (Reserved)
     * @param sid       SID (SID prefix 'I10')
     * @param sid_count How many 'I10's do we have
     */
    public HeaderMapping(long id, String t, String electrode,
                         double freq_low, double freq_high, String notes,
                         String sid, int sid_count) {
        this.id = id;
        this.type = t.trim();
        this.electrode = electrode.trim();
        this.freq_high = freq_high;
        this.freq_low = freq_low;
        this.sid = sid;
        this.count = sid_count;
        this.notes = notes.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getElectrode() {
        return electrode;
    }

    public void setElectrode(String electrode) {
        this.electrode = electrode;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getFreq_low() {
        return freq_low;
    }

    public void setFreq_low(double freq_low) {
        this.freq_low = freq_low;
    }

    public double getFreq_high() {
        return freq_high;
    }

    public void setFreq_high(double freq_high) {
        this.freq_high = freq_high;
    }
}
