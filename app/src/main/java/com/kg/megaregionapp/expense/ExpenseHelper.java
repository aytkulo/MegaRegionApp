package com.kg.megaregionapp.expense;

/**
 * Created by Aytkul Omurzakov on 7/9/2017.
 */

public class ExpenseHelper {


    public static String number2string(int number) {

        String numberString = "";
        int x = 0;

        if (number > 9999) {

            x = number / 1000;

            if (x > 9 && x < 21)
                numberString = twoDigit(x) + " тысяч ";
            else if (x % 10 == 1) {
                numberString = findString((x / 10) * 10) + " " + findString(-1) + " тысяча ";
            } else if (x % 10 == 2) {
                numberString = findString((x / 10) * 10) + " " + findString(-2) + " тысячи ";
            } else if (x % 10 >= 3 && x % 10 <= 4) {
                numberString = twoDigit(x) + " тысячи ";
            } else {
                numberString = twoDigit(x) + " тысяч ";
            }


            x = number % 1000;
            numberString = numberString + threeDigit(x);

        } else if (number > 999) {
            numberString = fourDigit(number);
        } else if (number > 99) {
            numberString = threeDigit(number);
        } else if (number > 20) {
            numberString = twoDigit(number);
        } else {
            numberString = findString(number);
        }

        if (number % 10 == 1)
            numberString = numberString + " сом";
        else
            numberString = numberString + " сомов";

        return numberString;
    }


    public static String fourDigit(int number) {
        String numberString = "";
        int temp = 0;
        int x = 0;
        x = number / 1000;

        if (x == 1) {
            numberString = findString(-1) + " тысяча";
        } else if (x >= 2 && x <= 4) {
            numberString = findString(-2) + " тысячи";
        } else {
            numberString = findString(x) + " тысяч";
        }

        x = number % 1000;

        if (x != 0) {
            temp = number % 1000;
            if (temp > 99)
                numberString = numberString + " " + threeDigit(temp);
            else if (temp > 20)
                numberString = numberString + " " + twoDigit(temp);
            else
                numberString = numberString + " " + findString(temp);

        }
        return numberString;
    }


    public static String threeDigit(int number) {
        String numberString = "";
        int temp = 0;
        int x = number % 100;

        if (x == 0) {
            numberString = findString(number);
        } else {
            temp = number % 100;

            x = number / 100;
            numberString = findString(x * 100);
            numberString = numberString + " " + twoDigit(temp);
        }
        return numberString;
    }

    public static String twoDigit(int number) {
        String numberString = "";

        if (number > 20) {
            int x = number % 10;

            if (x == 0) {
                numberString = findString(number);
            } else {
                x = number / 10;
                x = x * 10;
                numberString = findString(x);
                numberString = numberString + " " + findString(number % 10);
            }
        } else
            numberString = findString(number);

        return numberString;
    }

    public static String findString(int number) {

        String numberString = "";

        String[][] sayilar = {
                {"-2", "две"},
                {"-1", "одна"},
                {"1", "один"},
                {"2", "два"},
                {"3", "три"},
                {"4", "четыре"},
                {"5", "пять"},
                {"6", "шесть"},
                {"7", "семь"},
                {"8", "восемь"},
                {"9", "девять"},
                {"10", "десять"},
                {"11", "одиннадцать"},
                {"12", "двенадцать"},
                {"13", "тринадцать"},
                {"14", "четырнадцать"},
                {"15", "пятнадцать"},
                {"16", "шестнадцать"},
                {"17", "семнадцать"},
                {"18", "восемнадцать"},
                {"19", "девятнадцать"},
                {"20", "двадцать"},
                {"30", "тридцать"},
                {"40", "сорок"},
                {"50", "пятьдесят"},
                {"60", "шестьдесят"},
                {"70", "семьдесят"},
                {"80", "восемьдесят"},
                {"90", "девяносто"},
                {"100", "сто"},
                {"200", "двести"},
                {"300", "триста"},
                {"400", "четыреста"},
                {"500", "пятьсот"},
                {"600", "шестьсот"},
                {"700", "семьсот"},
                {"800", "восемьсот"},
                {"900", "девятьсот"}
        };


        for (int i = 0; i < sayilar.length; i++) {
            if (Integer.parseInt(sayilar[i][0]) == number) {
                numberString = sayilar[i][1];
                break;
            }
        }

        return numberString;
    }
}
