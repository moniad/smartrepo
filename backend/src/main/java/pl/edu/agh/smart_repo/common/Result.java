package pl.edu.agh.smart_repo.common;

public class Result {
    private ResultType type;
    private String message;
    private Exception exception;

    public Result(ResultType type) {
        this.type = type;
    }

    public Result(ResultType type, String message) {
        this.type = type;
        this.message = message;
    }

    public Result(ResultType type, Exception exception) {
        this.type = type;
        this.exception = exception;
    }

    public Result(ResultType type, String message, Exception exception) {
        this.type = type;
        this.message = message;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }

    public boolean isSuccess() {
        return type == ResultType.SUCCESS;
    }

    public boolean isFailure() {
        return type == ResultType.FAILURE;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
