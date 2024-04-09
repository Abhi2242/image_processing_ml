package com.smartgeek.imageprocessingtest.model

import android.os.Parcel
import android.os.Parcelable

data class PupilDistanceModel(
    val id: Int,
    val distance: Float
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeFloat(distance)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PupilDistanceModel> {
        override fun createFromParcel(parcel: Parcel): PupilDistanceModel {
            return PupilDistanceModel(parcel)
        }

        override fun newArray(size: Int): Array<PupilDistanceModel?> {
            return arrayOfNulls(size)
        }
    }
}