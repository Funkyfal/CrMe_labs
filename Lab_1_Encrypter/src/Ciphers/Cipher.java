package Ciphers;


import java.util.*;

import static java.lang.Math.abs;

public class Cipher {
    private String alphabet;
    private String inputText;
    private String key;
    private final int M;

    public Cipher(String alphabet, String inputText, String key) {
        this.alphabet = alphabet;
        this.key = key;
        this.inputText = inputText;
        M = alphabet.length();
    }

    public String shiftEncrypt() {
        if (key.length() != 1) {
            throw new IllegalArgumentException("Ключ состоит не из одного символа.");
        }

        int index = alphabet.indexOf(key);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < inputText.length(); i++) {
            int tempIndex = (alphabet.indexOf(inputText.charAt(i)) + index) % M;
            result.append(alphabet.charAt(tempIndex));
        }

        return result.toString();
    }

    public String shiftDecrypt() {
        if (key.length() != 1) {
            throw new IllegalArgumentException("Ключ состоит не из одного символа.");
        }

        int index = alphabet.indexOf(key);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < inputText.length(); i++) {
            int tempIndex = (alphabet.indexOf(inputText.charAt(i)) - index + M) % M;
            result.append(alphabet.charAt(tempIndex));
        }

        return result.toString();
    }

    public String affineEncrypt() {
        if (key.length() != 2 || GCD(alphabet.indexOf(key.charAt(0)), M) != 1) {
            throw new IllegalArgumentException("Либо неверна длина ключа(должна быть 2), либо первый символ ключа" +
                    "не взаимно прост с длиной алфавита.");
        }

        StringBuilder result = new StringBuilder();
        int k1 = alphabet.indexOf(key.charAt(0));
        int k2 = alphabet.indexOf(key.charAt(1));

        for (int i = 0; i < inputText.length(); i++) {
            int tempIndex = ((alphabet.indexOf(inputText.charAt(i)) * k1) + k2) % M;
            result.append(alphabet.charAt(tempIndex));
        }

        return result.toString();
    }

    public String affineDecrypt() {
        if (key.length() != 2 || GCD(alphabet.indexOf(key.charAt(0)), M) != 1) {
            throw new IllegalArgumentException("Либо неверна длина ключа(должна быть 2), либо первый символ ключа" +
                    "не взаимно прост с длиной алфавита.");
        }

        StringBuilder result = new StringBuilder();
        int k1 = alphabet.indexOf(key.charAt(0));
        int k2 = alphabet.indexOf(key.charAt(1));

        int k1Reverse = reverseNumber(k1, M);

        for (int i = 0; i < inputText.length(); i++) {
            int tempIndex = (k1Reverse * (alphabet.indexOf(inputText.charAt(i)) - k2 + M)) % M;
            result.append(alphabet.charAt(tempIndex));
        }
        return result.toString();
    }

    public String substitutionEncrypt() {
        if (key.length() != M) {
            throw new IllegalArgumentException("Текст в файле ключа имеет отличную от алфавита длину.");
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < inputText.length(); i++) {
            result.append(key.charAt(alphabet.indexOf(inputText.charAt(i))));
        }
        return result.toString();
    }

    public String substitutionDecrypt() {
        if (key.length() != M) {
            throw new IllegalArgumentException("Текст в файле ключа имеет отличную от алфавита длину.");
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < inputText.length(); i++) {
            result.append(alphabet.charAt(key.indexOf(inputText.charAt(i))));
        }
        return result.toString();
    }

    public String hillEncrypt() {
        int det = hillKeyCheck();
        int k11 = alphabet.indexOf(key.charAt(0));
        int k12 = alphabet.indexOf(key.charAt(1));
        int k21 = alphabet.indexOf(key.charAt(2));
        int k22 = alphabet.indexOf(key.charAt(3));

        if (inputText.length() % 2 == 1) {
            inputText += alphabet.charAt(0);
        }

        StringBuilder result = new StringBuilder();

        for (int i = 1; i < inputText.length(); i += 2) {
            int firstSymbol = alphabet.indexOf(inputText.charAt(i - 1));
            int secondSymbol = alphabet.indexOf(inputText.charAt(i));
            result.append(alphabet.charAt((firstSymbol * k11 + secondSymbol * k21) % M));
            result.append(alphabet.charAt((firstSymbol * k12 + secondSymbol * k22) % M));
        }
        return result.toString();
    }

    public String hillDecrypt() {
        int detR = reverseNumber(hillKeyCheck(), M); 
        int k11R = (detR * alphabet.indexOf(key.charAt(3)) + M) % M;
        int k12R = (detR * (-alphabet.indexOf(key.charAt(1)) + M)) % M;
        int k21R = (detR * (-alphabet.indexOf(key.charAt(2)) + M)) % M;
        int k22R = (detR * alphabet.indexOf(key.charAt(0)) + M) % M;

        if (inputText.length() % 2 == 1) {
            inputText += alphabet.charAt(0);
        }

        StringBuilder result = new StringBuilder();

        for (int i = 1; i < inputText.length(); i += 2) {
            int firstSymbol = alphabet.indexOf(inputText.charAt(i - 1));
            int secondSymbol = alphabet.indexOf(inputText.charAt(i));
            result.append(alphabet.charAt((firstSymbol * k11R + secondSymbol * k21R) % M));
            result.append(alphabet.charAt((firstSymbol * k12R + secondSymbol * k22R) % M));
        }
        return result.toString();
    }

    public int hillKeyCheck() {
        int k11, k12, k21, k22, det;
        if (key.length() == 4) {
            k11 = alphabet.indexOf(key.charAt(0));
            k12 = alphabet.indexOf(key.charAt(1));
            k21 = alphabet.indexOf(key.charAt(2));
            k22 = alphabet.indexOf(key.charAt(3));
            det = (((k11 * k22) % M) - ((k12 * k21) % M) + M) % M;

            if (det == 0) {
                throw new IllegalArgumentException("Матрица, состоящая из номеров символов в алфавите является" +
                        " вырожденной. Реализация метода невозможна.");
            }
            if (GCD(abs(det), M) != 1) {
                throw new IllegalArgumentException("Определитель матрицы не взаимно прост с длиной алфавита");
            }
            try {
                int detReverse = reverseNumber(det, M);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage() + ". Проверьте правильность ключа.");
            }
        } else {
            throw new IllegalArgumentException("Длина текста в key.txt != 4");
        }
        return det;
    }

    public String permutationEncrypt() {
        permutationCheck();
        Vector<Integer> keys = permutationPrepare();
        int neededSymbols = (inputText.length() % key.length() == 0) ? 0 : key.length()
                - inputText.length() % key.length();
        for (int i = 0; i < neededSymbols; i++) {
            inputText += alphabet.charAt(0);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < inputText.length(); i += key.length()) {
            String temp = inputText.substring(i, key.length() + i);
            for (int j = 0; j < key.length(); j++) {
                result.append(temp.charAt(keys.get(j) - 1));
            }
        }
        return result.toString();
    }

    public String permutationDecrypt() {
        permutationCheck();
        Vector<Integer> keys = permutationPrepare();
        int neededSymbols = (inputText.length() % key.length() == 0) ? 0 : key.length()
                - inputText.length() % key.length();
        for (int i = 0; i < neededSymbols; i++) {
            inputText += alphabet.charAt(0);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < inputText.length(); i += key.length()) {
            String temp = inputText.substring(i, key.length() + i);
            char[] decryptedBlock = new char[key.length()];
            for (int j = 0; j < key.length(); j++) {
                decryptedBlock[keys.get(j) - 1] = temp.charAt(j);
            }
            result.append(new String(decryptedBlock));
        }
        return result.toString();
    }

    public void permutationCheck() {
        if (key.length() > M) {
            throw new IllegalArgumentException("Длина ключа больше длины алфавита.");
        }
        Set<Character> keySet = new HashSet<>();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (!keySet.add(c)) {
                throw new IllegalArgumentException("Ключ содержит повторяющийся символ: " + c);
            }
        }
    }

    public Vector<Integer> permutationPrepare() {
        Vector<Integer> keys = new Vector<>();
        Set<Integer> keySet = new TreeSet<>();
        Vector<Integer> keysFinal = new Vector<>();
        for (int i = 0; i < key.length(); i++) {
            keySet.add(alphabet.indexOf(key.charAt(i)));
        }
        for (int i = 0; i < key.length(); i++) {
            keys.add(alphabet.indexOf(key.charAt(i)));
        }
        List<Integer> sortedKeyList = new ArrayList<>(keySet);
        for (int keyIndex : keys) {
            int sortedIndex = sortedKeyList.indexOf(keyIndex);
            keysFinal.add(sortedIndex + 1);
        }
        return keysFinal;
    }

    public String vigenereEncrypt() {
        int beginKeyLength = key.length();
        int j = 0;
        while (key.length() < inputText.length()) {
            if (j == beginKeyLength) {
                j = 0;
            }
            key += key.charAt(j);
            j++;
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < inputText.length(); i++) {
            result.append(alphabet.charAt((alphabet.indexOf(inputText.charAt(i)) + alphabet.indexOf(key.charAt(i))) % M));
        }

        return result.toString();
    }

    public String vigenereDecrypt() {
        int beginKeyLength = key.length();
        int j = 0;
        while (key.length() < inputText.length()) {
            if (j == beginKeyLength) {
                j = 0;
            }
            key += key.charAt(j);
            j++;
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < inputText.length(); i++) {
            result.append(alphabet.charAt((alphabet.indexOf(inputText.charAt(i)) - alphabet.indexOf(key.charAt(i)) + M) % M));
        }

        return result.toString();
    }

    private int reverseNumber(int a, int m) {
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1) {
                return x;
            }
        }
        throw new IllegalArgumentException("Невозможно подобрать обратное значение в кольце вычетов по модулю " + m);
    }

    public static int GCD(int a, int b) {
        int temp;
        while (b != 0) {
            if (a < b) {
                temp = a;
                a = b;
                b = temp;
            }
            temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }
}
