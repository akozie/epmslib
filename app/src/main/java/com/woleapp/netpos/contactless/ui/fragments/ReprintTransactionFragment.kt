package com.woleapp.netpos.contactless.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import com.netpluspay.netpossdk.NetPosSdk
//import com.netpluspay.netpossdk.printer.ReceiptBuilder
import com.woleapp.netpos.contactless.databinding.FragmentReprintTransactionsBinding

class ReprintTransactionFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentReprintTransactionsBinding.inflate(inflater, container, false)
//        val p = ReceiptBuilder(NetPosSdk.getPrinterManager(requireContext()).apply {
//            cleanCache()
//            setPrintGray(3000)
//            setLineSpace(1)
//        })
//            .apply {
//                appendAID("sample aid")
//                appendAddress("Oluwatayo Adegboye")
//                appendAmount("5000")
//                appendAppName("NetPOS")
//                appendAppVersion("1.5")
//                appendAuthorizationCode("Auth code")
//                appendCardHolderName("card holder name")
//                appendCardNumber("card number")
//                appendCardScheme("card scheme")
//                appendDateTime("Date time")
//                appendRRN("Sample RRN")
//                appendStan("Stan")
//                appendTerminalId("Terminal ID")
//                appendTransactionType("Trans New")
//                appendTransactionStatus("status")
//                appendResponseCode("00")
//            }.print().subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { _, _ ->
//
//            }
        return binding.root
    }
}