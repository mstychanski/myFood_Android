package com.android.myfood.home.storage

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.myfood.Constants
import com.android.myfood.Credencials
import com.android.myfood.R
import com.android.myfood.home.storage.item.SwipeBackgroundHelper
import com.android.myfood.home.storage.model.StorageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_product_info.*
import kotlinx.android.synthetic.main.dialog_product_info.view.*
import kotlinx.android.synthetic.main.fragment_storage.*
import kotlinx.android.synthetic.main.storage_item.*


class StorageFragment : Fragment(), StorageAdapter.OnItemClickListener {

    var fragmentView : View? = null
    var firedatabase : FirebaseDatabase? = null
    var itemList : ArrayList<StorageItem> ? = null
    private var ref : DatabaseReference ? = null

    var mRecyclerView : RecyclerView? = null

    lateinit var dialogBuilder: AlertDialog.Builder
    lateinit var dialog: AlertDialog

    var filterStatus: String? = ""


    private var adapter: StorageAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(

            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {


        fragmentView= LayoutInflater.from(activity).inflate(R.layout.fragment_storage, container, false)

        firedatabase = FirebaseDatabase.getInstance()

        mRecyclerView = fragmentView?.findViewById(R.id.recycler_view)
        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)
        itemList = arrayListOf<StorageItem>()



        ref = FirebaseDatabase.getInstance().getReference("Users").child(Credencials.USER_ID).child(Constants.DATABASE_ITEMS)


        ref?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, p0.getMessage())
            }

            override fun onDataChange(p0: DataSnapshot) {

                if (p0!!.exists()) {
                    if (itemList!!.isNotEmpty()) {
                        itemList!!.clear()
                    }
                    for (h in p0.children) {
                        val newItem = h.getValue(StorageItem::class.java)
                        if (newItem != null) {
                            newItem.keyId = h.key
                        }
                        //println("ItemId: "+newItem!!.keyId) //Checking key
                        itemList?.add(newItem!!)
                    }

                    adapter = StorageAdapter(context!!, itemList!!, this@StorageFragment)
                    adapter!!.showListByCatagory("all")

                    mRecyclerView?.addItemDecoration(MemberItemDecoration())
                    mRecyclerView?.setAdapter(adapter)

                }

                progressbar_storage?.visibility = View.GONE
                recycler_view?.visibility=View.VISIBLE
            }
        })

        return  fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.visibility = View.GONE;


        btn_add.setOnClickListener {
            val intent = Intent(this.activity, AddProductActivity::class.java)
            startActivity(intent)
        }

        toggle_group.setOnClickListener {

            val checkedUnit = form_unit_type.checkedButtonId
            val unit =
                    when (checkedUnit) {
                        R.id.toggle_freezer -> "freezer"
                        R.id.toggle_fridge -> "fridge"
                        R.id.toggle_pantry-> "pantry"
                        else -> "all"
                    }

        }
        val toggleClickListener = View.OnClickListener {

            val checkedUnit = toggle_group.checkedButtonId
                when (checkedUnit) {
                    R.id.toggle_freezer -> adapter!!.showListByCatagory("freezer")
                    R.id.toggle_fridge -> adapter!!.showListByCatagory("fridge")
                    R.id.toggle_pantry-> adapter!!.showListByCatagory("pantry")
                    else -> adapter!!.showListByCatagory("all")
                }
        }

        toggle_all.setOnClickListener(toggleClickListener)
        toggle_freezer.setOnClickListener(toggleClickListener)
        toggle_fridge.setOnClickListener(toggleClickListener)
        toggle_pantry.setOnClickListener(toggleClickListener)
    }





    var itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    var position = viewHolder.adapterPosition
                    var item = itemList!![position]
                    when(direction){
                        ItemTouchHelper.LEFT -> {
                            ContextCompat.getColor(requireContext(), R.color.red)
                            ref?.child(item.keyId!!)?.removeValue()
                            itemList!!.remove(item)
                            mRecyclerView!!.adapter!!.notifyItemRemoved(position)

                        }
                    }
                }

                override fun onChildDraw(
                        c: Canvas,
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        dX: Float,
                        dY: Float,
                        actionState: Int,
                        isCurrentlyActive: Boolean
                ) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val viewItem = viewHolder.itemView
                        SwipeBackgroundHelper.paintDrawCommandToStart(c, viewItem, R.drawable.ic_trash, dX)
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
    }


    override fun onItemClick(position: Int) {


        val DialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_product_info, null)

        val dialogName = DialogView?.findViewById<TextView>(R.id.dialog_product_info_name)
        val dialogWeight = DialogView?.findViewById<TextView>(R.id.dialog_product_info_weight)
        val dialogStoring = DialogView?.findViewById<TextView>(R.id.dialog_product_info_storage)
        val dialogExpiry = DialogView?.findViewById<TextView>(R.id.dialog_product_info_expire)

        val Builder = AlertDialog.Builder(activity)
                .setView(DialogView)

        dialogName?.text = itemList!![position].name
        dialogWeight?.text = itemList!![position].weight.toString() + "  " + itemList!![position].unit
        dialogStoring?.text = itemList!![position].storingPlace
        dialogExpiry?.text = itemList!![position].expiryDate

        val  AlertDialog = Builder.show()



        DialogView.dialogEditBtn.setOnClickListener {

            AlertDialog.dismiss()
        }

        DialogView.dialogCloseBtn.setOnClickListener {

            AlertDialog.dismiss()
        }
    }

    inner class MemberItemDecoration : RecyclerView.ItemDecoration ()
    {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            // only for the last one
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount!! - 1) {
                outRect.bottom = 120/* bottom margin after last item */
            }
        }

    }
}
