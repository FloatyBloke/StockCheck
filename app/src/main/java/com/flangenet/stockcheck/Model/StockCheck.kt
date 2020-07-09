package com.flangenet.stockcheck.Model

import android.os.Parcel
import android.os.Parcelable

class StockCheck (var checkID: Int, var productId: Int, var displayOrder: Int, var description: String, var stock: Float, var prep: Boolean, var changed:Boolean, var selected: Boolean) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(checkID)
        parcel.writeInt(productId)
        parcel.writeInt(displayOrder)
        parcel.writeString(description)
        parcel.writeFloat(stock)
        parcel.writeByte(if (prep) 1 else 0)
        parcel.writeByte(if (changed) 1 else 0)
        parcel.writeByte(if (selected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StockCheck> {
        override fun createFromParcel(parcel: Parcel): StockCheck {
            return StockCheck(parcel)
        }

        override fun newArray(size: Int): Array<StockCheck?> {
            return arrayOfNulls(size)
        }
    }
}