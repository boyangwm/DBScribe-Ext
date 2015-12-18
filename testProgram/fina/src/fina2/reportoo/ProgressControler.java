package fina2.reportoo;

public interface ProgressControler {

    public void incProgress();

    public void setProgress(int progress);

    public void setMaxProgress(int maxProgress);

    public void setMessage(String message);
}
