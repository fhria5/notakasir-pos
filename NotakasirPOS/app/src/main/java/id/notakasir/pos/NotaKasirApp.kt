package id.notakasir.pos

import android.app.Application
import id.notakasir.pos.data.repo.AppRepository

class NotaKasirApp : Application() {
    lateinit var repo: AppRepository
        private set
    override fun onCreate() {
        super.onCreate()
        repo = AppRepository(this)
    }
}
