package gr.algo.AlgoMobileServer.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDateTime

@Service
class CommunicationServiceImpl:CommunicationService {

    @Autowired
    @Qualifier("jdbcTemplate1")
    private val sqlite1: JdbcTemplate? = null

    @Autowired
    @Qualifier("jdbcTemplate2")
    private val sqlsrv1: JdbcTemplate? = null


    @Autowired
    @Qualifier("jdbcTemplate1")
    private val sqlite2: JdbcTemplate? = null

    @Autowired
    @Qualifier("jdbcTemplate2")
    private val sqlsrv2: JdbcTemplate? = null


    val comid=1
    val salesmanid=1


    override fun AndroidtoAtlantis() {
        fun(){TODO("Parameterise code mask")}


        //////////////////////////////////// CUSTOMER NEW
        sqlite1?.query("SELECT name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city," +
                " comments,routeid,id from customer where erpupd=0", object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet:ResultSet) {
                while (resultSet.next())
                {
                    fun(){TODO("Parameterise code mask")}
                    val code:String?=sqlsrv1?.queryForObject("SELECT TOP 1 CODE FROM customer where code like '0%' and comid="+comid.toString()+" order by code desc", String::class.java)?.padStart(4,'0')
                    sqlsrv1?.update("INSERT INTO CUSTOMER(name,street1,district1,identitynum,afm,doyid,occupation,phone11,phone12,fax1,email,fpastatus,city1,remarks,rotid,comid,curid,code) " +
                            "VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)+"','"+resultSet.getString(3)+"','"+resultSet.getString(4)+"','"+
                            resultSet.getString(5)+"','"+resultSet.getString(6)+"','"+resultSet.getString(7)+"','"+resultSet.getString(8)+"','"+
                            resultSet.getString(9)+"','"+resultSet.getString(10)+"','"+resultSet.getString(11)+"','"+resultSet.getString(12)+"','"+
                            resultSet.getString(13)+"','"+resultSet.getString(14)+"','"+resultSet.getString(15)+"',"+comid.toString()+",1,'"+code+"')")
                    val oldId=resultSet.getString(16)
                    sqlite2?.update("UPDATE customer set erpupd=1 where id="+oldId)
                    val newId=sqlsrv1?.queryForObject<String>("select id from customer where code='"+code+"' and comid="+comid)
                    sqlite2?.update("UPDATE customer set erpid="+newId+" where id="+oldId)
                    sqlite2?.update("UPDATE customer set id="+newId+" where id="+oldId)
                    sqlite2?.update("UPDATE cashtrn set perid="+newId+" where perid="+oldId)
                    sqlite2?.update("UPDATE fintrade set cusid="+newId+" where cusid="+oldId)





                }
            }
        })



        /////////////////////////??CUSTOMER CHANGE
        sqlite1?.query("SELECT name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city,comments,routeid,id from customer where erpupd=2", object: RowCallbackHandler{
            @Throws(SQLException::class)
            override fun processRow(resultSet:ResultSet) {
                while(resultSet.next()){
                    sqlsrv1?.update("UPDATE CUSTOMER SET name='"+ resultSet.getString(1)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET street1'"+ resultSet.getString(2)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET district1='"+ resultSet.getString(3)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET identitynum='"+ resultSet.getString(4)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET afm='"+ resultSet.getString(5)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET doyid="+ resultSet.getString(6)+" where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET occcupation'"+ resultSet.getString(7)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET phone11='"+ resultSet.getString(8)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET phone12='"+ resultSet.getString(9)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET fax1='"+ resultSet.getString(10)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET email='"+ resultSet.getString(11)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET fpastatus="+ resultSet.getString(12)+" where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET city1='"+ resultSet.getString(13)+"' where id="+resultSet.getString(16))
                    sqlsrv1?.update("UPDATE CUSTOMER SET remarks='"+ resultSet.getString(14)+"' where id="+resultSet.getString(16))

                    sqlite2?.update("UPDATE customer set erpupd=1 where id="+resultSet.getString(16)
)


                }
            }
        })


        /// SALES INVOICES

        sqlsrv1?.update("DELETE from z_fintrade")
        sqlsrv1?.update("DELETE from z_storetradelines")
        sqlsrv1?.update("DELETE from z_cash")
        var dsrId:Int?=0
        var ftrId:Int?=0
        sqlite1?.query("SELECT ftrdate,f.dsrid,f.dsrnumber,f.cusid,f.comments,f.deliveryaddress,f.netvalue,f.vatamount,f.totamount,f.id,c.vatstatusid from fintrade f,customer c where c.id=f.cusid and f.erpupd=0",
            object: RowCallbackHandler {
                @Throws(SQLException::class)
                override fun processRow(resultSet: ResultSet) {
                    while (resultSet.next()) {
                        val stdvatstatus=resultSet.getInt(11)
                        val tdate=resultSet.getDate(1).toString()


                        when(resultSet.getInt(2))
                        {
                            1->dsrId=sqlite2?.queryForObject<Int>("SELECT tdadsrid from z_pda where salesmanid="+salesmanid)
                            2->dsrId=sqlite2?.queryForObject<Int>("SELECT pisdsrid from z_pda where salesmanid="+salesmanid)
                            3->dsrId=sqlite2?.queryForObject<Int>("SELECT daddsrid from z_pda where salesmanid="+salesmanid)
                            4->dsrId=sqlite2?.queryForObject<Int>("SELECT depdsrid from z_pda where salesmanid="+salesmanid)
                            5->dsrId=sqlite2?.queryForObject<Int>("SELECT pagdsrid from z_pda where salesmanid="+salesmanid)
                        }

                        sqlsrv1?.update("INSERT INTO z_fintrade (ftrdate,dsrid,dsrnumber,cusid,comments,deliveryaddress,netvalue,vatamount,totamount,salesmanid,vatstatusid) values ('"
                                +tdate+"',"+dsrId.toString()+","+ resultSet.getInt(3).toString()+","+resultSet.getInt(4).toString()+",'"+ resultSet.getString(5)+"','"+resultSet.getString(6)
                                +"',"+resultSet.getFloat(7).toString()+","+resultSet.getFloat(8).toString()+","+resultSet.getFloat(9).toString()+","+salesmanid+","+stdvatstatus+")")
                        ftrId=sqlsrv1?.queryForObject<Int>("SELECT id from z_fintrade where ftrdate='"+tdate+"' and dsrid="+dsrId.toString()+" and dsrnumber="+resultSet.getInt(3).toString()+
                                " and cusid="+resultSet.getInt(4).toString())

                        sqlite2?.query("SELECT iteid,primaryqty,price,discount,discountpercent,linevalue,vatamount,vatid from storetradelines where ftrid="+resultSet.getInt(10).toString(),object: RowCallbackHandler {
                            @Throws(SQLException::class)
                            override fun processRow(resultSet1: ResultSet) {
                                while (resultSet1.next()) {
                                    var vtcId = when (stdvatstatus == 1) {
                                        true -> 99
                                        false -> resultSet1.getInt(8)
                                    }
                                    sqlsrv1?.update("INSERT INTO z_storetradelines(ftrid,iteid,primaryqty,price,discount,discountpercent,linevalue,vatamount,vatid) values (" + ftrId + "," + resultSet1.getInt(1).toString()
                                            + "," + resultSet1.getFloat(2).toString() + "," + resultSet1.getFloat(3).toString() + "," + resultSet1.getFloat(4).toString() + ","
                                            + resultSet1.getFloat(5).toString() + "," + resultSet1.getFloat(6).toString() + "," + resultSet1.getFloat(7).toString() + "," + vtcId.toString() + ")")
                                }
                            }
                        })

                        sqlite2?.query("SELECT trndate,perid,justification,amount from cashtrn where trntype=1 and ftrid="+resultSet.getInt(10).toString(),object: RowCallbackHandler {
                                @Throws(SQLException::class)
                                override fun processRow(resultSet2: ResultSet) {
                                    while (resultSet2.next()) {
                                        val dsrid=sqlite2?.queryForObject<Int>("SELECT metdsrid from z_pda where salesmanid="+salesmanid)
                                        val tdate=resultSet.getDate(1).toString()
                                        sqlsrv1?.update("INSERT INTO z_cash (cusid,amount,trndate,salesmanid,dsrid,ftrid,justification) values ("+resultSet2.getInt(2).toString()+","+
                                                resultSet2.getFloat(4).toString()+",'"+tdate+"',"+salesmanid+","+dsrid+","+ftrId+",'"+resultSet2.getString(3)+"')")
                                    }
                                }
                    })


                        }
                }
            })




        ///////////////////////////////////COLLECTIONS


        sqlite1?.query("SELECT trndate,perid,justification,amount from cashtrn where trntype=1 and ftrid is null",object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    val dsrid=sqlite2?.queryForObject<Int>("SELECT metdsrid from z_pda where salesmanid="+salesmanid)
                    val tdate=resultSet.getDate(1).toString()
                    sqlsrv1?.update("INSERT INTO z_cash(trndate,cusid,justification,amount,salesmanid,dsrid) values('"+tdate+"',"+resultSet.getInt(1).toString()+
                            ",'"+resultSet.getString(3)+"',"+resultSet.getFloat(4.toString())+","+salesmanid+","+dsrid+")")
                }
            }
        })



        ////////////// LOG

        sqlsrv1?.update("INSERT INTO z_log (task,backupfile,cdatetime) values (2,'algo.sqlite."+LocalDateTime.now().toString()+"-2',getdate())")


        ///
        sqlite1?.update("DELETE from fintrade ")
        sqlite1?.update("DELETE from storetradelines ")
        sqlite1?.update("DELETE from cashtrn ")




















    }


    override fun AtlantistoAndroid() {



        sqlite1?.update("VACUUM")



        /////MATERIAL
        sqlite1?.update("DELETE from material")
        sqlsrv1?.query("SELECT m.code,m.description,m.whsprice,m.vtcid,m.id,m.maxdiscount,ms.code from material m,mesunit ms where m.mu1=ms.codeid and m.isactive=1 and m.comid="+comid,object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO material (code,description,price,vatid,erpid,maxdiscount,unit) VALUES ("+resultSet.getString(1)+"','"+resultSet.getString(2)+"',"+resultSet.getFloat(3).toString()+","+resultSet.getInt(4).toString()
                            +","+resultSet.getInt(5).toString()+","+resultSet.getFloat(6).toString()+",'"+resultSet.getString(7).toString()+"')")
                }

            }
        })

        /////VAT
        sqlite1?.update("DELETE from vat")


        sqlsrv1?.query("SELECT v.codeid,v.percentage,vs.vtsid from vatcategory v,vatstatus vs where vs.vtnid=v.codeid",object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO vat (codeid,percent0,percent1) VALUES ('"+resultSet.getString(1)+"',"+resultSet.getFloat(2)+","+resultSet.getFloat(3).toString()+")")
                }

            }
        })



        /// CUSTOMER

        sqlite1?.update("DELETE from customer")


        sqlsrv1?.query("SELECT id,name,street1,district1,identitynum,afm,doyid,occupation,phone11,phone12,fax1,email,fpastatus, city1,remarks,rotid from customer where isactive=1 and comid="+comid,object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO customer (erpid,name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city,comments,routeid,erpupd) VALUES ('"+resultSet.getInt(1).toString()+
                            ",'" +resultSet.getString(2)+"','"+resultSet.getString(3) +"',"+resultSet.getString(4)+"','"+resultSet.getString(5)+"',"+resultSet.getInt(6).toString()+
                            ",'"+resultSet.getString(7)+"','"+resultSet.getString(8)+"','"+resultSet.getString(9)+"','"+resultSet.getString(10)+"','"+resultSet.getString(11)+
                            "','"+resultSet.getString(12)+"',"+resultSet.getInt(13).toInt()+",'"+resultSet.getString(14)+"','"+resultSet.getString(15)+"',"+resultSet.getInt(16).toString()+",1)"                    )
                }

            }
        })

        ////CUSTFINDATA

        sqlite1?.update("DELETE from custfindata")


        sqlsrv1?.query("SELECT masterid,masterbalance from custfindata",object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO custfindata (cusid,balance) VALUES ("+resultSet.getInt(1).toString()+"," +resultSet.getFloat(2).toString()+")")
                }

            }
        })





        ///DOY

        sqlite1?.update("DELETE from doy")


        sqlsrv1?.query("SELECT codeid,descr,code from doy",object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO doy(erpid,description,code) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"','"+resultSet.getString(3)+"')")
                }

            }
        })


        ///ROUTE

        sqlite1?.update("DELETE from route")


        sqlsrv1?.query("SELECT codeid,descr from route where comid="+comid,object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO route(erpid,description) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
                }

            }
        })

        ///PRDATA

        sqlite1?.update("DELETE from customerprices")


        sqlsrv1?.query("SELECT domainid1,domainid2,fld1 from prdata where comid="+comid,object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO customerprices (cusid,iteid,price) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+","+resultSet.getFloat(3).toString()+")")
                }

            }
        })




        ///SALESMAN

        sqlite1?.update("DELETE from salesman")


        sqlsrv1?.query("SELECT id,name from salesman where comid="+comid,object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO salesman (erpid,name) values ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
                }

            }
        })



        /////TABLETINFO


        sqlite1?.update("DELETE from salesman")


        sqlsrv1?.query("SELECT salesmanid,printermac from z_pda where salesmanid="+salesmanid,object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO tabletinfo (salesmanid,printercode) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
                }

            }
        })


        //////STOREFINDATA


        sqlite1?.update("DELETE from storefindata")

        val ftrid=sqlsrv1?.queryForObject<Int>("select max(id) from fintrade f where f.dsrid=sdadsrid and f.ftrdate=DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0) and f.colidsalesman="+salesmanid+" and f.comid="+comid)


        sqlsrv1?.query("select st.iteid,sum(st.primaryqty) from fintrade f,storetradelines st where st.ftrid=f.id and f.id="+ftrid+" group by st.iteid",object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO storefindata (iteid,startqty,qty) select id,"+resultSet.getFloat(2).toString(),","+resultSet.getFloat(2).toString()+
                            "from material where erpid="+resultSet.getInt(1).toString())

                }

            }
        })


        /////////DOCSERIES



        var codeId:Int=0
        var series:String=""
        var lastno:Int?=0

        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series from docseries d,z_pda z where d.codeid=z.tdadsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comid="+comid)
            { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
                series=rs.getString("series")
                lastno=rs.getInt("lastusednumber")
            }


        sqlite1?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=1")
        sqlite1?.update("UPDATE docseries set series='"+series+"'where id=1")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from fintrade where fyeid=year(getdate()) and domaintype=2 and comid="+comid+" and dsrid="+codeId+")")
        sqlite1?.update("UPDATE docseries set lastno='"+lastno+"'where id=1")



        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series from docseries d,z_pda z where d.codeid=z.pisdsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comid="+comid)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            lastno=rs.getInt("lastusednumber")
        }


        sqlite1?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=2")
        sqlite1?.update("UPDATE docseries set series='"+series+"'where id=2")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from fintrade where fyeid=year(getdate()) and domaintype=2 and comid="+comid+" and dsrid="+codeId+")")
        sqlite1?.update("UPDATE docseries set lastno='"+lastno+"'where id=2")



        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series from docseries d,z_pda z where d.codeid=z.dadsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comid="+comid)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            lastno=rs.getInt("lastusednumber")
        }


        sqlite1?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=3")
        sqlite1?.update("UPDATE docseries set series='"+series+"'where id=3")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from fintrade where fyeid=year(getdate()) and domaintype=2 and comid="+comid+" and dsrid="+codeId+")")
        sqlite1?.update("UPDATE docseries set lastno='"+lastno+"'where id=3")



        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series from docseries d,z_pda z where d.codeid=z.depdsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comid="+comid)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            lastno=rs.getInt("lastusednumber")
        }


        sqlite1?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=4")
        sqlite1?.update("UPDATE docseries set series='"+series+"'where id=4")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from fintrade where fyeid=year(getdate()) and domaintype=2 and comid="+comid+" and dsrid="+codeId+")")
        sqlite1?.update("UPDATE docseries set lastno='"+lastno+"'where id=4")


        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series from docseries d,z_pda z where d.codeid=z.pagdsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comid="+comid)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            lastno=rs.getInt("lastusednumber")
        }


        sqlite1?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=5")
        sqlite1?.update("UPDATE docseries set series='"+series+"'where id=5")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from fintrade where fyeid=year(getdate()) and domaintype=2 and comid="+comid+" and dsrid="+codeId+")")
        sqlite1?.update("UPDATE docseries set lastno='"+lastno+"'where id=5")






        ///COMPANYDATA


        sqlite1?.update("DELETE from companydata")


        sqlsrv1?.query("select name,occupation,address,city,afm,doy,tel1,tel2,email,site from z_companydata",object: RowCallbackHandler {
            @Throws(SQLException::class)
            override fun processRow(resultSet: ResultSet) {
                while (resultSet.next()) {
                    sqlite1?.update("INSERT INTO companydata (name,occupation,address,city,afm,doy,tel1,tel2,email,site) VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)
                            +"','"+resultSet.getString(3)+"','"+resultSet.getString(4)+"','"+resultSet.getString(5)+"','"+resultSet.getString(6)+"','"+
                            resultSet.getString(7)+"','"+resultSet.getString(8)+"','"+resultSet.getString(9)+"','"+resultSet.getString(10)+"')")
                }

            }
        })


        sqlsrv1?.update("INSERT INTO z_log (task,backupfile,cdatetime) values (1,'algo.sqlite.db values (1,algoosqlite.db"+LocalDateTime.now().toString()+"-1',getdate())-1',getdate())")

































    }


}