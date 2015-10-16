package org.ramonaza.unofficialazaapp.people.backend;


import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilanscheinkman on 3/14/15.
 */
public class ContactCSVHandler {


    private File file;
    private InputStream csvInputStream;

    public ContactCSVHandler(File file) throws FileNotFoundException {
        this.file = file;
        this.csvInputStream = new FileInputStream(file);
    }

    public ContactCSVHandler(InputStream inputStream) {
        this.csvInputStream = inputStream;
        this.file = null;
    }

    /**
     * Retrieves contacts from the file.
     *
     * @return the contacts from the CSV file in an array
     */
    public ContactInfoWrapper[] getCtactInfoListFromCSV() {
        List<String[]> cInfo = readContactInfoCsv();
        ContactInfoWrapper[] rval = new ContactInfoWrapper[cInfo.size()];
        for (int i = 0; i < cInfo.size(); i++) {
            rval[i] = createContactInfoWrapperFromCSVargs(cInfo.get(i));
        }
        return rval;
    }


    /**
     * Writes contacts to the CSV file in the downloads folder.
     *
     * @param toSave the contacts to save
     * @param append whether or not to append to the CSV or rewrite it
     * @return whether or not the writing succeeded
     */
    public boolean writesContactsToCSV(ContactInfoWrapper[] toSave, boolean append) {
        if (file == null) return false;
        String dataToWrite = "";
        for (ContactInfoWrapper contact : toSave) {
            dataToWrite += contact.getName() + "," + contact.getSchool() + "," + contact.getGradYear() + ",\"" + contact.getAddress() + "\","
                    + contact.getLatitude() + "," + contact.getLongitude() + "," + contact.getEmail() + "," + contact.getPhoneNumber() + "\n";
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(file, append);
            outputStream.write(dataToWrite.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    private List<String[]> readContactInfoCsv() {
        List<String[]> contactCSVline = new ArrayList<String[]>();
        try {
            InputStreamReader csvStreamReader = new InputStreamReader(csvInputStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                contactCSVline.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactCSVline;
    }


    private ContactInfoWrapper createContactInfoWrapperFromCSVargs(String[] args) {
        ContactInfoWrapper rRapper = new ContactInfoWrapper();
        if (args.length <= 6) {
            rRapper.setName(args[0]);
            rRapper.setSchool(args[1]);
            rRapper.setGradYear(args[2]);
            rRapper.setAddress(args[3]);
            rRapper.setLongitude(0);
            rRapper.setLatitude(0);
            rRapper.setEmail(args[4]);
            rRapper.setPhoneNumber(args[5]);
        } else {
            rRapper.setName(args[0]);
            rRapper.setSchool(args[1]);
            rRapper.setGradYear(args[2]);
            rRapper.setAddress(args[3]);
            rRapper.setLatitude(args[4]);
            rRapper.setLongitude(args[5]);
            rRapper.setEmail(args[6]);
            rRapper.setPhoneNumber(args[7]);
        }
        return rRapper;
    }
}
