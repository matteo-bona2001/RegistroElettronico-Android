package com.sharpdroid.registroelettronico.viewModels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sharpdroid.registroelettronico.database.entities.Grade
import com.sharpdroid.registroelettronico.database.entities.LocalGrade
import com.sharpdroid.registroelettronico.database.pojos.AverageType
import com.sharpdroid.registroelettronico.database.pojos.SubjectPOJO
import com.sharpdroid.registroelettronico.database.room.DatabaseHelper

class SubjectDetailsViewModel : ViewModel() {
    private var averages: LiveData<List<AverageType>>? = null
    var grades: LiveData<List<Grade>>? = null
    private var localGrades: LiveData<List<LocalGrade>>? = null
    var subjectInfo: LiveData<SubjectPOJO>? = null
    var animateTarget = MutableLiveData<Boolean>()
    var animateLocalMarks = MutableLiveData<Boolean>()
    private var profile = Array<Long>(4, { 0 })

    fun getAverages(profile: Long, subject: Long, period: Int): LiveData<List<AverageType>> {
        if (averages == null || this.profile[0] != profile) {
            averages = if (period != -1) DatabaseHelper.database.gradesDao().getPeriodTypeAverage(profile, subject, period) else DatabaseHelper.database.gradesDao().getAllTypeAverage(profile, subject)
            this.profile[0] = profile
        }
        return averages ?: throw NullPointerException("Averages' liveData not yet initialized")
    }

    fun getGrades(profile: Long, subject: Long, period: Int): LiveData<List<Grade>> {
        if (grades == null || this.profile[1] != profile) {
            grades = if (period != -1) DatabaseHelper.database.gradesDao().periodGrades(profile, subject, period) else DatabaseHelper.database.gradesDao().allPeriodsGrades(profile, subject)
            this.profile[1] = profile
        }
        return grades ?: throw NullPointerException("Grades' liveData not yet initialized")
    }

    fun getLocalGrades(profile: Long, subject: Long, period: Int): LiveData<List<LocalGrade>> {
        if (localGrades == null || this.profile[2] != profile) {
            localGrades = if (period != -1) DatabaseHelper.database.gradesDao().periodLocalGrades(profile, subject, period) else DatabaseHelper.database.gradesDao().allPeriodsLocalGrades(profile, subject)
            this.profile[2] = profile
        }
        return localGrades ?: throw NullPointerException("Averages' liveData not yet initialized")
    }

    fun getSubject(subject: Long, profile: Long): LiveData<SubjectPOJO> {
        if (subjectInfo == null || this.profile[3] != profile) {
            subjectInfo = DatabaseHelper.database.subjectsDao().getSubjectPOJO(subject, profile)
            this.profile[3] = profile
        }
        return subjectInfo ?: throw NullPointerException("Subject liveData not yet initialized")
    }
}