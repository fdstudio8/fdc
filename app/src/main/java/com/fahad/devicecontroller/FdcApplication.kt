package com.fahad.devicecontroller

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.fahad.devicecontroller.core.detection.CompatibilityDetector
import com.fahad.devicecontroller.core.policy.PersistentPolicyManager
import com.fahad.devicecontroller.core.remote.GitHubRemoteConfigManager
import com.fahad.devicecontroller.core.security.AdminAuthManager
import com.fahad.devicecontroller.dpc.DevicePolicyBridge
import com.fahad.devicecontroller.enrollment.EnrollmentWorkflowManager
import com.fahad.devicecontroller.feature.inventory.AppInventoryManager
import com.fahad.devicecontroller.feature.notes.NotesManager
import com.fahad.devicecontroller.oem.OemExtensionManager

/**
 * Global Application Context & Dependency Graph for FDC.
 */
class FdcApplication : Application() {

    lateinit var policyBridge: DevicePolicyBridge
        private set

    lateinit var oemManager: OemExtensionManager
        private set

    lateinit var compatibilityDetector: CompatibilityDetector
        private set

    lateinit var adminAuthManager: AdminAuthManager
        private set

    lateinit var persistentPolicyManager: PersistentPolicyManager
        private set

    lateinit var gitHubRemoteConfigManager: GitHubRemoteConfigManager
        private set

    lateinit var enrollmentWorkflowManager: EnrollmentWorkflowManager
        private set

    lateinit var notesManager: NotesManager
        private set

    lateinit var appInventoryManager: AppInventoryManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        policyBridge = DevicePolicyBridge(this)
        oemManager = OemExtensionManager(this)
        compatibilityDetector = CompatibilityDetector(this, policyBridge, oemManager)
        adminAuthManager = AdminAuthManager(this)
        persistentPolicyManager = PersistentPolicyManager(this, policyBridge)
        gitHubRemoteConfigManager = GitHubRemoteConfigManager(this, persistentPolicyManager)
        enrollmentWorkflowManager = EnrollmentWorkflowManager(this)
        notesManager = NotesManager(this)
        appInventoryManager = AppInventoryManager(this, policyBridge)

        // Launch continuous background GitHub sync (every 60s)
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        gitHubRemoteConfigManager.startContinuousSync(appScope, 60L)
    }

    companion object {
        lateinit var instance: FdcApplication
            private set
    }
}
