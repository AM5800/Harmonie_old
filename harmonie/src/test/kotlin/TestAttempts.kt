import common.AttemptsHistoryManagerBase
import common.util.OutputStreamWrapper
import model.util.InputStreamWrapper
import java.io.FileInputStream

public class TestAttempts : AttemptsHistoryManagerBase() {
    override fun write(writer: (OutputStreamWrapper) -> Unit) {
    }

    override fun append(writer: (OutputStreamWrapper) -> Unit) {
    }

    override fun read(reader: (InputStreamWrapper) -> Unit) {
        FileInputStream("..\\data\\attempts_15.06.15.dat").use { s -> reader(InputStreamWrapper(s)) }
    }
}

