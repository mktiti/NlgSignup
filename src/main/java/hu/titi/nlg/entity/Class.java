package hu.titi.nlg.entity;

public final class Class implements Comparable<Class> {

    public enum Year {
        NINE((short)9), TEN((short)10), ELEVEN((short)11), TWELVE((short)12);

        public final short value;

        Year(short value) {
            this.value = value;
        }

        static Year of(int year) {
            if (year >= 9 && year <= 12) {
                return values()[year - 9];
            }
            return null;
        }

        @Override
        public String toString() {
            return Short.toString(value);
        }
    }

    public enum Sign {
        A, B, C, D;

        static Sign of(char sign) {
            if (sign >= 'A' && sign <= 'D') {
                return values()[sign - 'A'];
            }
            return null;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public final Year year;
    public final Sign sign;

    public Class(Year year, Sign sign) {
        this.year = year;
        this.sign = sign;
    }

    public static Class of(int year, String sign) {
        return (sign.length() == 1) ? of(year, sign.charAt(0)) : null;
    }

    private static Class of(int year, char sign) {
        Class c = new Class(Year.of(year), Sign.of(sign));
        return (c.year == null || c.sign == null) ? null : c;
    }

    public Year getYear() {
        return year;
    }

    public Sign getSign() {
        return sign;
    }

    @Override
    public int compareTo(Class aClass) {
        return year.compareTo(aClass.year);
    }

    @Override
    public String toString() {
        return year + "." + sign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Class aClass = (Class) o;

        return year == aClass.year && sign == aClass.sign;
    }

    @Override
    public int hashCode() {
        int result = year != null ? year.hashCode() : 0;
        result = 31 * result + (sign != null ? sign.hashCode() : 0);
        return result;
    }
}