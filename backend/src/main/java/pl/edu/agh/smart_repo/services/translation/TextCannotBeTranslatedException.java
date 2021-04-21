package pl.edu.agh.smart_repo.services.translation;

public class TextCannotBeTranslatedException extends RuntimeException {

    public TextCannotBeTranslatedException() {
        super("Translation API have not translated text correctly. Check your Internet connection." +
                " If it is stable probably daily limit of words was exceeded.");
    }
}
