package org.ramonazaapi.contacts;


import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

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

    private static ContactInfoWrapper createContactInfoWrapperFromCSVargs(String[] args) {
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

    private static String createCsvLineFromContact(ContactInfoWrapper contact) {
        return contact.getName() + "," + contact.getSchool() + "," + contact.getGradYear() + ",\"" + contact.getAddress() + "\","
                + contact.getX() + "," + contact.getY() + "," + contact.getEmail() + "," + contact.getPhoneNumber() + "\n";

    }

    /**
     * Retrieves contacts from the file.
     *
     * @return the contacts from the CSV file in an array
     */
    public Observable<ContactInfoWrapper> getCtactInfoListFromCSV() {
        return readContactInfoCsv().map(new Func1<String[], ContactInfoWrapper>() {
            @Override
            public ContactInfoWrapper call(String[] strings) {
                return createContactInfoWrapperFromCSVargs(strings);
            }
        });
    }

    /**
     * Writes contacts to the CSV file in the downloads folder.
     *
     * @param toSave the contacts to save
     * @param append whether or not to append to the CSV or rewrite it
     * @return whether or not the writing succeeded
     */
    public Observable<Boolean> writesContactsToCSV(Collection<ContactInfoWrapper> toSave, final boolean append) {
        if (file == null)
            return Observable.error(new IllegalStateException("No CSV file currently loaded."));
        return Observable.from(toSave).map(new Func1<ContactInfoWrapper, String>() {
            @Override
            public String call(ContactInfoWrapper contactInfoWrapper) {
                return createCsvLineFromContact(contactInfoWrapper);
            }
        }).collect(new Func0<StringBuilder>() {

            @Override
            public StringBuilder call() {
                return new StringBuilder();
            }
        }, new Action2<StringBuilder, String>() {
            @Override
            public void call(StringBuilder stringBuilder, String s) {
                stringBuilder.append(s);
            }
        }).map(new Func1<StringBuilder, String>() {
            @Override
            public String call(StringBuilder stringBuilder) {
                return stringBuilder.toString();
            }
        }).map(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(file, append);
                    outputStream.write(s.getBytes());
                    outputStream.flush();
                    outputStream.close();
                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        });
    }

    private Observable<String[]> readContactInfoCsv() {
        return Observable.create(new Observable.OnSubscribe<String[]>() {
            @Override
            public void call(Subscriber<? super String[]> subscriber) {
                try {
                    InputStreamReader csvStreamReader = new InputStreamReader(csvInputStream);
                    CSVReader csvReader = new CSVReader(csvStreamReader);
                    String[] line;
                    while ((line = csvReader.readNext()) != null) {
                        subscriber.onNext(line);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
