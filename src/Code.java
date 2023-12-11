public enum Code {
    CONNECT_CODE("CONNECT"),
    DISCONNECT_CODE("DISCONNECT"),
    MESSAGE_CODE("MESSAGE"),
    LIST_CODE("LIST"),
    WHISPER_CODE("WHISPER"),
    HELP_CODE("HELP");

    private String code;

    Code(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
