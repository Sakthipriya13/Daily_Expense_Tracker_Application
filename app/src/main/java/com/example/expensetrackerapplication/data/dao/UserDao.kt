package com.example.expensetrackerapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.expensetrackerapplication.data.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT userId FROM User WHERE UserEmail=:email")
    suspend fun fnGetSignUpUserId(email: String?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun fnInsertUser(user: UserEntity) : Long

    @Query("SELECT * FROM User WHERE UserName=:name AND UserPassword=:password")
    suspend fun fnGetUserBasedOnUserName(name: String?, password: String?): List<UserEntity>


    @Query("SELECT * FROM User WHERE UserName=:name")
    suspend fun fnGetUserBasedOnUserName(name: String?): List<UserEntity>

    @Query("DELETE FROM User WHERE userId= :id")
    suspend fun fnDeleteUserAccountFromDb(id: Int) : Int

    @Query("UPDATE USER SET UserPassword= :newPassword , IsSynced=0 WHERE UserId= :userId AND UserPassword= :currentPassword")
    suspend fun fnUpdateUserPassword(newPassword: String?, userId: Int, currentPassword: String?) : Int

    @Query("UPDATE USER SET UserPassword= :newPassword , IsSynced=0  WHERE UserEmail= :email")
    suspend fun fnResetUserPassword(newPassword: String?,email:String?) : Int

//    @Query("UPDATE USER SET UserProfilePhotoUri= :uri WHERE UserId= :userId")
//    suspend fun fnUpdateUserProfilePhoto(uri: String?, userId: Int) : Int

//    @Query("SELECT UserProfilePhotoUri FROM User WHERE userId= :userId")
//    suspend fun fnGetUserProfilePhotoUri(userId : Int) : String

    @Query("SELECT COUNT(*) FROM USER" )
    suspend fun fnGetUsersCount(): Int

//    @Query("UPDATE User SET UserProfilePhotoUri= :imageUri WHERE CloudId= :cloudId")
//    suspend fun fnUpdateProfileUri(imageUri: String,cloudId : String)

//    @Query("SELECT UserProfilePhotoUri FROM User WHERE userId= :userId AND CloudId =:cloudId")
//    suspend fun fnGetProfileUri(cloudId : String,userId : Int):String

    @Query("SELECT * FROM USER WHERE IsSynced=0 AND userId= :lUserId")
    suspend  fun fnGetUnSyncedCurUserDetails(lUserId: Int): List<UserEntity>

    @Update
    suspend  fun fnUpdateCurUserDetails(userId : UserEntity)

    @Query("SELECT * FROM User WHERE UserEmail = :email")
    suspend fun isEmailExistsFun(email: String?): List<UserEntity>


}