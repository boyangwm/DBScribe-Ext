package tada;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TaDaMethod {
	String[] variablesToTrack();
	String[] correspondingDatabaseAttribute();
}
