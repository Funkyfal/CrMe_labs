import Ciphers.Cipher;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/*
* Для того, чтобы запустить программу, для начала нужно выставить нужные аргументы. Первый аргумент
* есть целое число от 1 до 6. таким образом выбирается один из шифров
* 1.Цезаря
* 2.Афинный
* 3.Простой замены
* 4.Хилла
* 5.Перестановки (простой)
* 6.Виженера
* Далее идет число 0 или 1, в зависимости от того, хотите Вы зашифровать (0) или расшифровать (1) сообщение.
* Перед запуском программы нужно написать исходные текст, ключ и алфавит. В случае отсутствия файла с алфавитом
* будет использоваться алфавит по умолчанию "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ ". В случае других ошибок ввода
* ключа, текста или самого алфавита, программа будет завершаться с указанием причины закрытия.
* */
public class Main {

    public static void main(String[] args) {
        int cipherChoice = Integer.parseInt(args[0]);
        int EncOrDec = Integer.parseInt(args[1]);
        if (args.length != 2 || cipherChoice < 1 || cipherChoice > 6 || (EncOrDec != 1 && EncOrDec != 0)) {
            throw new IllegalArgumentException("Проверьте входные данные");
        }

        BufferedReader inputReader = null;
        BufferedReader keyReader = null;
        BufferedReader alphabetReader = null;
        BufferedWriter cryptWriter = null;
        BufferedWriter decryptWriter = null;

        String alphabet;
        String inputText;
        String key;

        try {
            inputReader = openBufferedReader("in.txt");
            inputText = readSingleLine(inputReader, "in.txt");

            keyReader = openBufferedReader("key.txt");
            key = readSingleLine(keyReader, "key.txt");

            File alphabetFile = new File("alphabet.txt");
            if (alphabetFile.exists()) {
                alphabetReader = openBufferedReader("alphabet.txt");
                alphabet = readSingleLine(alphabetReader, "alphabet.txt");
            } else {
                System.out.println("Файл alphabet.txt не найден. Будет использован алфавит по умолчанию.");
                alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ ";
            }

            cryptWriter = openBufferedWriter("crypt.txt", false);
            decryptWriter = openBufferedWriter("decrypt.txt", false);

            allTextCheck(alphabet, inputText, key);
            Cipher cipher = new Cipher(alphabet, inputText, key);

            switch (cipherChoice) {
                case 1:
                    System.out.println("Выбран метод сдвига");
                    if (EncOrDec == 0) {
                        cryptWriter.append(cipher.shiftEncrypt());
                        System.out.println("Шифровка...");
                    } else {
                        decryptWriter.append(cipher.shiftDecrypt());
                        System.out.println("Расшифровка...");
                    }
                    break;
                case 2:
                    System.out.println("Выбран аффинный метод");
                    if (EncOrDec == 0) {
                        cryptWriter.append(cipher.affineEncrypt());
                        System.out.println("Шифровка...");
                    } else {
                        decryptWriter.append(cipher.affineDecrypt());
                        System.out.println("Расшифровка...");
                    }
                    break;
                case 3:
                    System.out.println("Выбран метод простой замены");
                    if (EncOrDec == 0) {
                        cryptWriter.append(cipher.substitutionEncrypt());
                        System.out.println("Шифровка...");
                    } else {
                        decryptWriter.append(cipher.substitutionDecrypt());
                        System.out.println("Расшифровка...");
                    }
                    break;
                case 4:
                    System.out.println("Выбран метод Хилла");
                    if (EncOrDec == 0) {
                        cryptWriter.append(cipher.hillEncrypt());
                        System.out.println("Шифровка...");
                    } else {
                        decryptWriter.append(cipher.hillDecrypt());
                        System.out.println("Расшифровка...");
                    }
                    break;
                case 5:
                    System.out.println("Выбран метод перестановки");
                    if (EncOrDec == 0) {
                        cryptWriter.append(cipher.permutationEncrypt());
                        System.out.println("Шифровка...");
                    } else {
                        decryptWriter.append(cipher.permutationDecrypt());
                        System.out.println("Расшифровка...");
                    }
                    break;
                case 6:
                    System.out.println("Выбран метод Виженера");
                    if (EncOrDec == 0) {
                        cryptWriter.append(cipher.vigenereEncrypt());
                        System.out.println("Шифровка...");
                    } else {
                        decryptWriter.append(cipher.vigenereDecrypt());
                        System.out.println("Расшифровка...");
                    }
                    break;
                default:
                    break;
            }

            System.out.println("Используемый алфавит: " + alphabet);
            System.out.println("Входной текст: " + inputText);
            System.out.println("Ключ: " + key);

        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } finally {
            try {
                if (inputReader != null) inputReader.close();
                if (keyReader != null) keyReader.close();
                if (alphabetReader != null) alphabetReader.close();
                if (cryptWriter != null) cryptWriter.close();
                if (decryptWriter != null) decryptWriter.close();
            } catch (IOException e) {
                System.err.println("Ошибка закрытия файлов: " + e.getMessage());
            }
        }
        System.out.println("Программа завершена успешно");
    }

    public static BufferedReader openBufferedReader(String fileName) throws IOException {
        try {
            return new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Не найден файл: " + fileName);
        }
    }

    public static BufferedWriter openBufferedWriter(String fileName, boolean append) throws IOException {
        try {
            return new BufferedWriter(new FileWriter(fileName, append));
        } catch (IOException e) {
            throw new IOException("Ошибка при открытии файла для записи: " + fileName);
        }
    }

    public static String readSingleLine(BufferedReader reader, String fileName) throws IOException {
        String line = reader.readLine();
        if (reader.readLine() != null) {
            throw new IOException("Файл " + fileName + " содержит больше одной строки!");
        }
        return line;
    }

    public static void allTextCheck(String alphabet, String inputText, String key) {
        Set<Character> alphabetSet = new HashSet<>();
        for (int i = 0; i < alphabet.length(); i++) {
            char c = alphabet.charAt(i);
            if (!alphabetSet.add(c)) {
                throw new IllegalArgumentException("Алфавит содержит повторяющийся символ: " + c);
            }
        }

        for (int i = 0; i < inputText.length(); i++) {
            char c = inputText.charAt(i);
            if (!alphabetSet.contains(c)) {
                throw new IllegalArgumentException("Символ '" + c + "' из файла с текстом не найден в алфавите.");
            }
        }

        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!alphabetSet.contains(c)) {
                throw new IllegalArgumentException("Символ '" + c + "' из файла с ключом не найден в алфавите.");
            }
        }
    }
}
