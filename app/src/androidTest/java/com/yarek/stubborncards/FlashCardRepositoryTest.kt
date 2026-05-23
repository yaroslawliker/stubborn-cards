import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yarek.stubborncards.database.AppDatabase
import com.yarek.stubborncards.database.repository.FlashCardRepository
import com.yarek.stubborncards.model.ProgressLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlashCardRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: FlashCardRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getInstance(context)
        repository = FlashCardRepository(context)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun createCard_automaticallyInitializesCorrectProgress() = runTest {
        repository.createCardWithInitialProgress("Stubborn", "Упертий")

        val counts = repository.progressLevelCounts.first()
        assertEquals(1, counts[ProgressLevel.NEW])
    }
}