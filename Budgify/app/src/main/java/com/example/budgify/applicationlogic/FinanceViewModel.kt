package com.example.budgify.applicationlogic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.budgify.entities.Account
import com.example.budgify.entities.Category
import com.example.budgify.entities.DefaultCategories
import com.example.budgify.entities.Loan
import com.example.budgify.entities.LoanType
import com.example.budgify.entities.MyTransaction
import com.example.budgify.entities.Objective
import com.example.budgify.entities.ObjectiveType
import com.example.budgify.entities.TransactionType
import com.example.budgify.userpreferences.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _snackbarMessages = MutableSharedFlow<String>() // Use SharedFlow for events
    val snackbarMessages: Flow<String> = _snackbarMessages.asSharedFlow() // Expose as Flow

    // TRANSACTIONS
//    val allTransactions = repository.getAllTransactions().stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000),
//        emptyList()
//    )
    val allTransactionsWithDetails = repository.getAllTransactionsWithDetails().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    fun addTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(myTransaction)
            repository.updateAccountBalance(myTransaction.accountId)
        }
    }
    suspend fun updateTransaction(myTransaction: MyTransaction) {
        val oldTransaction = withContext(Dispatchers.IO) {
            repository.getTransactionById(myTransaction.id)
        }
        // val oldTransaction = repository.getTransactionById(myTransaction.id)
        //Log.d("FinanceViewModel", "Old Transaction: $oldTransaction")
        //Log.d("FinanceViewModel", "Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
        // Update the transaction in the database
        repository.updateTransaction(myTransaction)
        //Log.d("FinanceViewModel", "New Transaction: $myTransaction")
        // Now, after the transaction is updated, update the account balance(s)
        if (oldTransaction != null) {
            //Log.d("FinanceViewModel", "Old Transaction is not null")
            if (oldTransaction.accountId != myTransaction.accountId) {
                //Log.d("FinanceViewModel", "Account changed")
                // Account changed: Update balance of old and new accounts
                withContext(Dispatchers.IO) {
                    repository.updateAccountBalance(oldTransaction.accountId)
                    repository.updateAccountBalance(myTransaction.accountId)
                }
                //Log.d("FinanceViewModel", "1-Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
            } else {
                //Log.d("FinanceViewModel", "Account is the same")
                // Account is the same: Update balance of the current account
                withContext(Dispatchers.IO) {
                    repository.updateAccountBalance(myTransaction.accountId)
                }
                //Log.d("FinanceViewModel", "2-Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
            }
        } else {
            //Log.d("FinanceViewModel", "Old Transaction is null")
            // This case is unexpected for an update, but if the old transaction
            // wasn't found, we can still try to update the balance of the
            // account the transaction is currently associated with.
            withContext(Dispatchers.IO) {
                repository.updateAccountBalance(myTransaction.accountId)
            }
            //Log.d("FinanceViewModel", "3-Old Balance: ${repository.getAccountById(myTransaction.accountId)?.amount.toString()}")
        }
    }
    fun deleteTransaction(myTransaction: MyTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(myTransaction)
            repository.updateAccountBalance(myTransaction.accountId)
        }
    }

    // OBJECTIVES
    val allObjectives = repository.getAllObjectives().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    fun addObjective(objective: Objective) {
        viewModelScope.launch {
            repository.insertObjective(objective)
        }
    }
    fun updateObjective(objective: Objective) {
        viewModelScope.launch {
            repository.updateObjective(objective)
        }
    }
    fun deleteObjective(objective: Objective) {
        viewModelScope.launch {
            repository.deleteObjective(objective)
        }
    }

    // --- XP AND LEVEL SYSTEM ---

    // Add StateFlows for user's level and XP
    // These would typically be loaded from and saved to a data source (e.g., DataStore, Room UserProfile table)
    private val _userLevel = MutableStateFlow(1) // Default to level 1
    val userLevel: StateFlow<Int> = _userLevel.asStateFlow()

    private val _userXp = MutableStateFlow(0) // XP towards next level
    val userXp: StateFlow<Int> = _userXp.asStateFlow()

    // You'd also need a way to get the XP required for the current level
    val xpForCurrentUserNextLevel: StateFlow<Int> = _userLevel.map { level ->
        calculateXpForNextLevel(level) // Use the same helper
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateXpForNextLevel(1))

    private val _unlockedThemeNames = MutableStateFlow<Set<String>>(emptySet())
    val unlockedThemeNames: StateFlow<Set<String>> = _unlockedThemeNames.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // Load initial values from DataStore
                val loadedLevel = repository.userLevel.first()
                _userLevel.value = loadedLevel // .first() gets the first emitted value
                _userXp.value = repository.userXp.first()
                Log.d("XP_System", "Initial Level: ${_userLevel.value}, Initial XP: ${_userXp.value} loaded from DataStore.")
                repository.unlockedThemes.collect { themesFromDataStore ->
                    val themesUnlockedByLevel = AppTheme.entries
                        .filter { it.unlockLevel <= loadedLevel } // Use the just loaded level
                        .map { it.name }
                        .toSet()

                    // Combine and update the StateFlow
                    _unlockedThemeNames.value = themesFromDataStore + themesUnlockedByLevel // Set union
                    Log.d("ThemeUnlock_Init", "Unlocked themes initialized. DataStore: $themesFromDataStore, LevelBased: $themesUnlockedByLevel, Combined: ${_unlockedThemeNames.value}")
                }
            } catch (e: Exception) {
                Log.e("XP_System", "Error loading initial level/XP from DataStore", e)
                _userLevel.value = 1
                _userXp.value = 0

                // Fallback for themes if everything else fails
                // Ensure themes for level 1 are at least set if there was an error
                val defaultThemesForLevel1 = AppTheme.entries
                    .filter { it.unlockLevel <= _userLevel.value } // Uses potentially reset _userLevel.value
                    .map { it.name }
                    .toSet()
                _unlockedThemeNames.value = defaultThemesForLevel1
                Log.d("ThemeUnlock_Init", "Error loading, defaulted themes to: ${_unlockedThemeNames.value} based on level ${_userLevel.value}")
            }
        }
    }

    private suspend fun ensureThemesForCurrentLevelAreStored() {
        val currentLvl = _userLevel.value
        val currentUnlockedInVm = _unlockedThemeNames.value // What VM thinks is unlocked

        AppTheme.entries.forEach { theme ->
            if (theme.unlockLevel <= currentLvl && !currentUnlockedInVm.contains(theme.name)) {
                // This theme should be unlocked by level, but isn't in our current VM's set (and thus maybe not in DataStore)
                try {
                    repository.addUnlockedTheme(theme.name) // This will save and also update the flow _unlockedThemeNames listens to
                    Log.d("ThemeUnlock_Ensure", "Ensured ${theme.name} is saved as unlocked for level $currentLvl.")
                } catch (e: Exception) {
                    Log.e("ThemeUnlock_Ensure", "Failed to save ${theme.name} during ensure check.", e)
                }
            }
        }
    }

    fun completeLoanAndCreateTransaction(
        loan: Loan,
        accountId: Int,
        // categoryId: Int? = null,
        // onCompletionFeedback: ((newLevel: Int, newlyUnlockedTheme: AppTheme?) -> Unit)? = null
    ) { // Added optional categoryId
        Log.d("XP_DEBUG", "completeObjectiveAndCreateTransaction called for objective: ${loan.desc}, accountId: $accountId")
        viewModelScope.launch {
            if (!loan.completed) {
                // 1. Mark loan as completed
                val updatedLoan = loan.copy(completed = true)
                repository.updateLoan(updatedLoan) // You already have this in your repository

                // 2. Create a transaction
                val transactionType: TransactionType
                val defaultCategoryDescription: String
                when (loan.type) {
                    LoanType.DEBT -> { // You paid off a debt, so it's an EXPENSE transaction
                        transactionType = TransactionType.EXPENSE
                        defaultCategoryDescription = DefaultCategories.DEBT_EXP.desc // e.g., "Loan Repayment Made"
                    }
                    LoanType.CREDIT -> { // Someone paid you back, so it's an INCOME transaction
                        transactionType = TransactionType.INCOME
                        defaultCategoryDescription = DefaultCategories.CREDIT_INC.desc // e.g., "Loan Repayment Received"
                    }
                }

                Log.d("VM_LoanCategoryFetch", "ViewModel looking for loan category description: '$defaultCategoryDescription'")

                val defaultCategory = withContext(Dispatchers.IO) {
                    repository.getCategoryByDescription(defaultCategoryDescription)
                }
                val categoryIdForTransaction = defaultCategory?.id

                if (categoryIdForTransaction == null) {
                    Log.e("FinanceViewModel", "Could not find default category for loan: $defaultCategoryDescription. Transaction for loan '${loan.desc}' will have no category.")
                    _snackbarMessages.emit("Error: Default category '$defaultCategoryDescription' not found for loan. Transaction created without category.")
                }

                val newTransaction = MyTransaction(
                    // id will be auto-generated by Room
                    accountId = accountId, // This will come from the UI
                    type = transactionType,
                    date = LocalDate.now(), // Or objective.endDate, if preferred
                    description = "Loan: ${loan.desc}",
                    amount = loan.amount,
                    categoryId = categoryIdForTransaction // Pass the selected categoryId, or null
                )
                // Use your existing addTransaction function which also handles updating account balance
                addTransaction(newTransaction)

                val xpGained = calculateXpForLoanCompletion(loan)
                addXp(xpGained, null)

                // The UI should react to changes in allObjectives and allTransactionsWithDetails StateFlows
                // No explicit refresh needed here if your UI collects these flows.
                Log.d("XP_System", "Loan '${loan.desc}' completed. XP Gained: $xpGained. Current XP: ${_userXp.value}/${xpForCurrentUserNextLevel.value}, Level: ${_userLevel.value}")
            } else {
                Log.d("XP_System", "Loan '${loan.desc}' was already completed.")
            }
        }
    }


    fun completeObjectiveAndCreateTransaction(
        objective: Objective,
        accountId: Int,
        // categoryId: Int? = null,
        // onCompletionFeedback: ((newLevel: Int, newlyUnlockedTheme: AppTheme?) -> Unit)? = null
    ) { // Added optional categoryId
        Log.d("XP_DEBUG", "completeObjectiveAndCreateTransaction called for objective: ${objective.desc}, accountId: $accountId")
        viewModelScope.launch {
            if (!objective.completed) {
                // 1. Mark objective as completed
                val updatedObjective = objective.copy(completed = true)
                repository.updateObjective(updatedObjective) // You already have this in your repository

                // 2. Determine TransactionType and fetch corresponding default Category ID
                val transactionType: TransactionType
                val defaultCategoryDescription: String

                when (objective.type) {
                    ObjectiveType.INCOME -> {
                        transactionType = TransactionType.INCOME
                        defaultCategoryDescription = DefaultCategories.OBJECTIVES_INC.desc // from your DefaultCategories object
                    }
                    ObjectiveType.EXPENSE -> {
                        transactionType = TransactionType.EXPENSE
                        defaultCategoryDescription = DefaultCategories.OBJECTIVES_EXP.desc // from your DefaultCategories object
                    }
                }

                // Fetch the category ID from the repository
                // Ensure your CategoryDao has a method like getCategoryByDescriptionSuspend
                val defaultCategory = withContext(Dispatchers.IO) { // Perform DB operation off the main thread
                    repository.getCategoryByDescription(defaultCategoryDescription)
                }

                val categoryIdForTransaction = defaultCategory?.id // Use the fetched category's ID

                if (categoryIdForTransaction == null) {
                    Log.e("FinanceViewModel", "Could not find default category: $defaultCategoryDescription. Transaction for goal '${objective.desc}' will have no category.")
                    // Optionally, emit a snackbar message about the missing category
                    _snackbarMessages.emit("Error: Default category '$defaultCategoryDescription' not found. Transaction created without category.")
                }

                // 3. Create a transaction

                val newTransaction = MyTransaction(
                    // id will be auto-generated by Room
                    accountId = accountId, // This will come from the UI
                    type = transactionType,
                    date = LocalDate.now(), // Or objective.endDate, if preferred
                    description = "Goal: ${objective.desc}",
                    amount = objective.amount,
                    categoryId = categoryIdForTransaction // Pass the selected categoryId, or null
                )
                // Use your existing addTransaction function which also handles updating account balance
                addTransaction(newTransaction)

                // 4. Add XP
                val xpGained = calculateXpForObjective(objective)
                addXp(xpGained, null)

                // The UI should react to changes in allObjectives and allTransactionsWithDetails StateFlows
                // No explicit refresh needed here if your UI collects these flows.
                Log.d("XP_System", "Goal '${objective.desc}' reached. XP Gained: $xpGained. Current XP: ${_userXp.value}/${xpForCurrentUserNextLevel.value}, Level: ${_userLevel.value}")
            } else {
                Log.d("XP_System", "Goal '${objective.desc}' was already completed.")
            }
        }
    }

    private fun addXp(
        amount: Int,
        onLevelUpCallback: ((newLevel: Int, newlyUnlockedTheme: AppTheme?) -> Unit)? = null
    ) {
        viewModelScope.launch { // The entire body should be in a coroutine scope for repository call
            Log.d("XP_DEBUG_ADDXP", "addXp called with amount: $amount. Current XP: ${_userXp.value}, Current Level: ${_userLevel.value}")

            if (amount <= 0) {
                Log.d("XP_DEBUG_ADDXP", "Amount is <= 0, returning.")
                return@launch
            }

            var tempXp = _userXp.value + amount
            var tempLevel = _userLevel.value
            var xpNeededForNext = calculateXpForNextLevel(tempLevel)
            var hasLeveledUpThisGain = false
            var highestLevelReachedThisGain = tempLevel
            var newlyUnlockedThemeDuringGain: AppTheme? = null

            while (tempXp >= xpNeededForNext) {
                tempXp -= xpNeededForNext
                tempLevel++
                xpNeededForNext = calculateXpForNextLevel(tempLevel)
                hasLeveledUpThisGain = true
                highestLevelReachedThisGain = tempLevel // Keep track of the highest new level

                Log.d("XP_System", "Level Up! New Level: $tempLevel")
                _snackbarMessages.emit("Level Up! You are now Level $tempLevel!")
                // Check for theme unlock at this new level
                val themeUnlockedNow = AppTheme.entries.find { theme ->
                    theme.unlockLevel == tempLevel && !_unlockedThemeNames.value.contains(theme.name)
                }

                if (themeUnlockedNow != null) {
                    repository.addUnlockedTheme(themeUnlockedNow.name) // Save to DataStore (this should trigger the flow collection in init/elsewhere to update _unlockedThemeNames)
                    _snackbarMessages.emit("New Theme Unlocked: ${themeUnlockedNow.displayName}!")
                    if (newlyUnlockedThemeDuringGain == null) {
                        newlyUnlockedThemeDuringGain = themeUnlockedNow
                    }
                }
            }

            // Update StateFlows with final calculated values
            val oldLevelValue = _userLevel.value
            _userLevel.value = tempLevel
            _userXp.value = tempXp

            Log.d("XP_DEBUG_ADDXP", "StateFlows updated: _userLevel=${_userLevel.value}, _userXp=${_userXp.value}")

            // --- SAVE TO DATASTORE ---
            try {
                repository.updateUserLevelAndXp(_userLevel.value, _userXp.value)
                Log.d("XP_DEBUG_ADDXP", "Successfully saved Level/XP to DataStore.")

                // After saving level, ensure any themes for this new level (or past levels if missed) are noted
                // This is a good place if addUnlockedTheme in the loop above relies on the flow updating _unlockedThemeNames
                // or if you want to be absolutely sure.
                // Alternatively, the collection of repository.unlockedThemes in init should handle this.
                // For robustness, you could call a specific function:
                // ensureThemesForCurrentLevelAreStored() // Call this to make sure DataStore is consistent

            } catch (e: Exception) {
                Log.e("XP_DEBUG_ADDXP", "Error saving Level/XP to DataStore", e)
            }

            if (hasLeveledUpThisGain) {
                // Potentially call ensureThemesForCurrentLevelAreStored() here as well if you want to be certain
                // that by the time onLevelUp is called, _unlockedThemeNames reflects all themes for the new level.
                // However, the collection in init and the addUnlockedTheme in the loop should handle it.
                onLevelUpCallback?.invoke(highestLevelReachedThisGain, newlyUnlockedThemeDuringGain)
            }
        }
    }


    private fun checkForThemeUnlock(newLevel: Int): AppTheme? {
        val currentUnlocked = unlockedThemeNames.value // Get current unlocked themes
        return AppTheme.entries.find { theme ->
            theme.unlockLevel == newLevel && !currentUnlocked.contains(theme.name)
        }
    }

    // XP Calculation Logic for Loans
    private fun calculateXpForLoanCompletion(loan: Loan): Int {
        var baseXP = 0

        // 1. XP based on amount
        //    (e.g., 1 XP for every 15 currency units for loans, min 8 XP for amount part)
        val amountXp = maxOf(8, (loan.amount / 15).toInt())
        baseXP += amountXp
        Log.d("XP_Calc", "Loan '${loan.desc}': Amount XP = $amountXp")

        // 2. Bonus XP based on loan type (optional)
        when (loan.type) {
            LoanType.DEBT -> { // Successfully paid off a debt
                val debtClearBonus = 20 // Higher satisfaction bonus for clearing debt
                baseXP += debtClearBonus
                Log.d("XP_Calc", "Loan '${loan.desc}': Debt clear bonus = $debtClearBonus")
            }
            LoanType.CREDIT -> { // Successfully collected a credit
                val creditCollectBonus = 10
                baseXP += creditCollectBonus
                Log.d("XP_Calc", "Loan '${loan.desc}': Credit collect bonus = $creditCollectBonus")
            }
            // else -> { /* Handle other potential LoanTypes if you add them */ }
        }

        // 3. Optional: Bonus if the loan had a due date and was settled on time or early
        //    This would require your `Loan` entity to have `dueDate: LocalDate?` and possibly `creationDate: LocalDate?`
        //    For example:
           if (loan.endDate != null) {
                val today = LocalDate.now()
                val daysRemaining = ChronoUnit.DAYS.between(today, loan.endDate)
                if (daysRemaining >= 0) { // Paid on or before due date
                    val onTimeBonus = (baseXP * 0.05).toInt() // Small bonus for on-time payment
                    baseXP += onTimeBonus
                    Log.d("XP_Calc", "Loan '${loan.desc}': On-time/early bonus = $onTimeBonus")
                } else {
                    Log.d("XP_Calc", "Loan '${loan.desc}': Paid late, no on-time bonus.")
                }
            }

        val finalXP = maxOf(15, baseXP) // Ensure a minimum XP gain for any loan completion (e.g., 15 XP)
        Log.d("XP_Calc", "Loan '${loan.desc}': Total XP Awarded = $finalXP")
        return finalXP
    }

    // XP Calculation Logic
    private fun calculateXpForObjective(objective: Objective): Int {
        var baseXP = 0

        // 1. XP based on amount (example: 1 XP for every 10 currency units, min 5 XP for amount part)
        val amountXp = maxOf(5, (objective.amount / 10).toInt())
        baseXP += amountXp

        // 2. Bonus XP for early completion
        val today = LocalDate.now()
        val daysRemaining = ChronoUnit.DAYS.between(today, objective.endDate)
        val totalDuration = ChronoUnit.DAYS.between(objective.startDate, objective.endDate)

        // Ensure no division by zero or negative totalDuration if start and end date are same or illogical
        if (totalDuration <= 0) { // If objective duration is 0 or 1 day, or invalid
            if (daysRemaining >= 0) { // Completed on time or early for a short objective
                baseXP += (baseXP * 0.1).toInt() // Small flat bonus (e.g., 10%)
            }
        } else {
            if (daysRemaining < 0) { // Completed late
                // No bonus, or even a penalty if desired (e.g., baseXP /= 2)
                Log.d("XP_System", "Goal completed late. Days remaining: $daysRemaining")
            } else { // Completed on time or early
                // Bonus proportional to how much of the objective's duration was left
                val earlyCompletionRatio = daysRemaining.toDouble() / totalDuration.toDouble()
                // Max bonus of, say, 50% of baseXP for completing very early
                val earlyCompletionBonus = (earlyCompletionRatio * (baseXP * 0.5)).toInt()
                baseXP += earlyCompletionBonus
                Log.d("XP_System", "Goal completed on time/early. Days remaining: $daysRemaining, Total duration: $totalDuration, Bonus: $earlyCompletionBonus")
            }
        }

        // Ensure a minimum XP is always awarded for any completed objective
        return maxOf(10, baseXP) // Example: minimum 10 XP
    }

    // Calculates XP needed to reach the *next* level from the given *current* level
    private fun calculateXpForNextLevel(level: Int): Int {
        // Example formula: 100 base XP for level 1 to 2, then increasing
        // (currentLevel -1) * 50 means difficulty scales up by 50xp more per level
        if (level <= 0) return 100 // Should not happen with current logic starting at level 1
        return 100 * level + (level - 1) * 50
    }


    //CATEGORIES
    val allCategories = repository.getAllCategories().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    private val defaultCategoryDescriptions = setOf(
        DefaultCategories.OBJECTIVES_EXP.desc,
        DefaultCategories.OBJECTIVES_INC.desc,
        DefaultCategories.CREDIT_EXP.desc,
        DefaultCategories.CREDIT_INC.desc,
        DefaultCategories.DEBT_EXP.desc,
        DefaultCategories.DEBT_INC.desc
    )
    suspend fun isDefaultCategory(categoryId: Int?): Boolean {
        if (categoryId == null) return false

        // This will now call the suspend fun FinanceRepository.getCategoryById(Int)
        // which in turn calls suspend fun CategoryDao.getCategoryById(Int)
        val category = repository.getCategoryByIdNonFlow(categoryId)
        return category?.desc in defaultCategoryDescriptions
    }
    val categoriesForTransactionDialog: Flow<List<Category>> = allCategories.map { categories ->
        val defaultCategoryDescriptions = setOf(
            DefaultCategories.OBJECTIVES_EXP.desc,
            DefaultCategories.OBJECTIVES_INC.desc,
            DefaultCategories.CREDIT_EXP.desc,
            DefaultCategories.CREDIT_INC.desc,
            DefaultCategories.DEBT_EXP.desc,
            DefaultCategories.DEBT_INC.desc
        )
        categories.filterNot { category ->
            // Check if the category's description is in our set of default descriptions
            defaultCategoryDescriptions.contains(category.desc)
        }
    }
    fun addCategory(category: Category, onCategoryCreated: (Category) -> Unit) {
        viewModelScope.launch {
            val newId = repository.insertCategory(category) // This now returns Long
            if (newId != -1L) { // Check if insert was successful (Room returns -1 on conflict if OnConflictStrategy.IGNORE fails to insert)
                val createdCategory = category.copy(id = newId.toInt())
                onCategoryCreated(createdCategory)
            } else {
                // Handle the case where the category was not inserted
                // (e.g., if a category with the same unique properties already exists and IGNORE prevented insertion)
                // You might want to fetch the existing category and pass it to the callback,
                // or signal an error, depending on desired behavior.
                // For now, we'll assume successful insertion or it's handled by IGNORE.
                // If you need to get the existing one:
                // val existingCategory = repository.getCategoryByDescription(category.desc)
                // if (existingCategory != null) {
                //     onCategoryCreated(existingCategory)
                // } else {
                //     // Log error or some other handling
                // }
                Log.w("FinanceViewModel", "Category '${category.desc}' might not have been inserted (newId: $newId). It might already exist.")
                // Optionally, try to fetch it by description if newId is -1 to handle IGNORE cases.
                val existingCategory = repository.getCategoryByDescription(category.desc)
                if (existingCategory != null) {
                    onCategoryCreated(existingCategory)
                } else {
                    // Log an error or handle as appropriate if it truly wasn't inserted and cannot be found.
                    Log.e("FinanceViewModel", "Failed to insert or find existing category: ${category.desc}")
                }
            }
        }
    }
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // ACCOUNTS
    val allAccounts = repository.getAllAccounts().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    fun addAccount(account: Account) {
        viewModelScope.launch {
            repository.insertAccount(account)
        }
    }
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }
    val hasAccounts: Flow<Boolean> = allAccounts.map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false // Or true, depending on your initial state logic
        )

    /**
     * Updates an account's title and initial amount, then recalculates its current balance
     * based on all its associated transactions.
     *
     * @return true if the update and recalculation were successful, false otherwise.
     */
    suspend fun updateAccountAndRecalculateBalance(
        accountId: Int,
        newTitle: String,
        newInitialAmount: Double
    ): Boolean {
        return viewModelScope.async { // Usa async se vuoi restituire un risultato
            // 1. Recupera l'account esistente (opzionale, potresti passare l'account intero)
            val accountToUpdate = repository.getAccountById(accountId) // Dovrai aggiungere getAccountById al DAO e Repository
            if (accountToUpdate == null) {
                Log.e("FinanceViewModel", "Account with ID $accountId not found for update.")
                return@async false
            }

            // 2. Recupera tutte le transazioni per questo account
            //    Assicurati che transactionsByAccountId restituisca un List<MyTransaction> e non un Flow per questo uso specifico,
            //    oppure raccogli il primo valore dal Flow.
            val transactionsForAccount = repository.getTransactionsForAccount(accountId) // Dovrai creare questo metodo

            // 3. Calcola il delta delle transazioni
            var transactionsDelta = 0.0
            transactionsForAccount.forEach { transaction ->
                if (transaction.type == TransactionType.INCOME) {
                    transactionsDelta += transaction.amount
                } else {
                    transactionsDelta -= transaction.amount
                }
            }

            // 4. Calcola il nuovo 'amount' (saldo corrente)
            val newCurrentAmount = newInitialAmount + transactionsDelta

            // 5. Crea l'oggetto Account aggiornato
            val updatedAccount = accountToUpdate.copy(
                title = newTitle,
                initialAmount = newInitialAmount,
                amount = newCurrentAmount // Imposta l'amount ricalcolato
            )

            // 6. Aggiorna l'account nel database
            repository.updateAccount(updatedAccount)
            Log.d("FinanceViewModel", "Account $accountId updated. New Initial: $newInitialAmount, New Current: $newCurrentAmount")
            true
        }.await() // Aspetta il risultato dell'operazione asincrona
    }


    // LOANS --- Nuova sezione per i Prestiti ---
    val allLoans: StateFlow<List<Loan>> = repository.getAllLoans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalActiveCreditLoans: StateFlow<Double> = allLoans
        .map { loans ->
            loans.filter { it.type == LoanType.CREDIT && !it.completed } // Filtra per TIPO e NON COMPLETATI
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalActiveDebtLoans: StateFlow<Double> = allLoans
        .map { loans ->
            loans.filter { it.type == LoanType.DEBT && !it.completed } // Filtra per TIPO e NON COMPLETATI
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val latestActiveLoans: StateFlow<List<Loan>> = allLoans
        .map { loans ->
            // Il DAO già ordina per startDate DESC, quindi prendiamo solo i primi 3
            loans.takeLast(6).reversed()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedCreditLoansCount: StateFlow<Int> = allLoans
        .map { loans ->
            loans.count { it.type == LoanType.CREDIT && it.completed }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val completedDebtLoansCount: StateFlow<Int> = allLoans
        .map { loans ->
            loans.count { it.type == LoanType.DEBT && it.completed }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun addLoan(loan: Loan, accountId: Int) {
        viewModelScope.launch {
            // 1. Insert the loan itself
            repository.insertLoan(loan)
            _snackbarMessages.emit("${loan.type.name.lowercase().replaceFirstChar { it.titlecase() }} '${loan.desc}' added.")


            // 2. Determine TransactionType and default Category ID based on LoanType
            val transactionType: TransactionType
            val defaultCategoryDescription: String

            when (loan.type) {
                LoanType.CREDIT -> { // You are lending money, so it's an EXPENSE from your account
                    transactionType = TransactionType.EXPENSE
                    defaultCategoryDescription = DefaultCategories.CREDIT_EXP.desc // e.g., "Loan Given" or similar
                }
                LoanType.DEBT -> {   // You are borrowing money, so it's an INCOME to your account
                    transactionType = TransactionType.INCOME
                    defaultCategoryDescription = DefaultCategories.DEBT_INC.desc // e.g., "Loan Received" or similar
                }
            }

            // 3. Fetch the default category
            val defaultCategory = withContext(Dispatchers.IO) {
                repository.getCategoryByDescription(defaultCategoryDescription)
            }
            val categoryIdForTransaction = defaultCategory?.id

            if (categoryIdForTransaction == null) {
                Log.e("FinanceViewModel_AddLoan", "Could not find default category: $defaultCategoryDescription. Transaction for new loan '${loan.desc}' will have no category.")
                _snackbarMessages.emit("Warning: Default category '$defaultCategoryDescription' not found. Transaction created without category.")
            }

            // 4. Create and insert the corresponding transaction
            val newTransaction = MyTransaction(
                // id will be auto-generated by Room
                accountId = accountId, // Use the provided accountId
                type = transactionType,
                date = loan.startDate, // Use the loan's start date for the transaction
                description = "Loan: ${loan.desc}", // Link the transaction to the loan
                amount = loan.amount,
                categoryId = categoryIdForTransaction
            )

            // Use your existing addTransaction function which also handles updating account balance
            // Make sure addTransaction (or repository.insertTransaction and repository.updateAccountBalance)
            // are suspend functions or are called within a viewModelScope.launch.
            // Your current addTransaction is already a viewModelScope.launch, which is good.
            addTransaction(newTransaction) // This will also update the account balance

            Log.d("FinanceViewModel_AddLoan", "Loan '${loan.desc}' added and corresponding transaction created for account ID $accountId.")
            // You might want a specific snackbar message for the transaction creation as well,
            // or combine it with the loan added message.
            // _snackbarMessages.emit("Transaction for ${loan.type.name.lowercase()} '${loan.desc}' created.")
        }
    }

    fun updateLoan(loan: Loan) {
        viewModelScope.launch {
            repository.updateLoan(loan)
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }
    // --- Fine sezione LOANS ---

    /**
     * Provides a StateFlow map of categories to their total expense amounts for a given account.
     * The map is sorted by amount in descending order.
     */
    fun getExpenseDistributionForAccount(accountId: Int): StateFlow<Map<Category, Double>> {
        return allTransactionsWithDetails
            .map { transactionsWithDetailsList ->
                transactionsWithDetailsList
                    .filter { it.transaction.accountId == accountId &&
                            it.transaction.type == TransactionType.EXPENSE &&
                            it.category != null } // Filter for expenses of the account with a category
                    .groupBy { it.category!! } // Group by the Category object
                    .mapValues { entry ->
                        // Sum the amounts of transactions in each category group
                        entry.value.sumOf { transactionWithDetail -> transactionWithDetail.transaction.amount }
                    }
                    .toList() // Convert to list of pairs for sorting
                    .sortedByDescending { it.second } // Sort by amount descending
                    .toMap() // Convert back to a map (LinkedHashMap preserves order)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap() // Initial value while loading or if no data
            )
    }

    fun getIncomeDistributionForAccount(accountId: Int): StateFlow<Map<Category, Double>> {
        return allTransactionsWithDetails
            .map { transactionsWithDetailsList ->
                transactionsWithDetailsList
                    .filter { it.transaction.accountId == accountId &&
                            it.transaction.type == TransactionType.INCOME &&
                            it.category != null } // Filter for incomes of the account with a category
                    .groupBy { it.category!! } // Group by the Category object
                    .mapValues { entry ->
                        // Sum the amounts of transactions in each category group
                        entry.value.sumOf { transactionWithDetail -> transactionWithDetail.transaction.amount }
                    }
                    .toList() // Convert to list of pairs for sorting
                    .sortedByDescending { it.second } // Sort by amount descending
                    .toMap() // Convert back to a map (LinkedHashMap preserves order)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyMap() // Initial value while loading or if no data
            )
    }

    fun resetUserProgressForTesting() { // New public function for UI to call
        viewModelScope.launch {
            try {
                // Reset level and XP
                repository.resetUserLevelAndXp()
                _userLevel.value = 1 // Update local StateFlow
                _userXp.value = 0    // Update local StateFlow
                Log.d("DevReset", "User level and XP reset to defaults.")

                // Reset unlocked themes
                repository.resetUnlockedThemes()
                // The collection in init should update _unlockedThemeNames,
                // but for immediate UI feedback, we can also force it:
                val defaultThemes = AppTheme.entries
                    .filter { it.unlockLevel <= 1 }
                    .map { it.name }
                    .toSet()
                _unlockedThemeNames.value = defaultThemes
                Log.d("DevReset", "Unlocked themes reset to defaults: $defaultThemes")
                _snackbarMessages.emit("User progress has been reset.")


                // You might also want to reset the currently selected theme to a default
                // This depends on how ThemePreferenceManager is accessed or if ViewModel manages it.
                // For now, this focuses on level and unlockable themes.

            } catch (e: Exception) {
                Log.e("DevReset", "Error resetting user progress", e)
                _snackbarMessages.emit("Error resetting progress.") // Optional: notify UI of error
                // Optionally, provide feedback to the UI about the error
            }
        }
    }

    // Factory per creare l'istanza del ViewModel
    class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}