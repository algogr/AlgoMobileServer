package gr.algo.AlgoMobileServer.service

import gr.algo.AlgoMobileServer.AlgoMobileServerApplication
import gr.algo.AlgoMobileServer.context
import net.bytebuddy.utility.JavaModule.ofType
import org.apache.juli.logging.Log
import org.omg.CORBA.Environment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import javax.annotation.PostConstruct
import kotlin.system.exitProcess

@Service
class CommunicationServiceImpl:CommunicationService {

    @Autowired
    @Qualifier("jdbcTemplate1")
    private val sqlite1: JdbcTemplate? = null

    @Autowired
    @Qualifier("jdbcTemplate2")
    private val sqlsrv1: JdbcTemplate? = null


    @Autowired
    @Qualifier("jdbcTemplate3")
    private val sqlite2: JdbcTemplate? = null

    @Autowired
    @Qualifier("jdbcTemplate2")
    private val sqlsrv2: JdbcTemplate? = null

    @Autowired
    @Qualifier("jdbcTemplate1")
    private val sqlite3: JdbcTemplate? = null





    @Value("\${algo.kavoukis.customercodemask}")
    val codeMask:String?=""
    @Value("\${algo.kavoukis.comid}")
    var comId:Int?=null
    @Value("\${algo.kavoukis.salesmanid}")
    var salesmanid:Int?=null
    @Value("\${algo.kavoukis.customerpricesquery}")
    var pricesQuery:String=""
    @Value("\${algo.kavoukis.materialcodemask}")
    val materialMask:String?=""
    @Value("\${algo.kavoukis,tableprefix}")
    val tablePrefix:String?=""




    override fun AndroidtoAtlantis() {




        sqlite1?.query("SELECT name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city," +
                " comments,5,id from customer where erpid=0")
            {resultSet->
                do

                {

                    val code:String?=(Integer.parseInt(sqlsrv1?.queryForObject("SELECT TOP 1 CODE FROM customer where code like '$codeMask%' and comId="+comId.toString()+" order by code desc", String::class.java))+1).toString().padStart(4,'0')
                    val doy={if(resultSet.getInt(6)==0) "NULL" else resultSet.getInt(6).toString()}
                    sqlsrv1?.update("INSERT INTO CUSTOMER(name,street1,district1,identitynum,afm,doyid,occupation,phone11,phone12,fax1,email,fpastatus,city1,remarks,rotid,comId,curid,code) " +
                            "VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)+"','"+resultSet.getString(3)+"','"+resultSet.getString(4)+"','"+
                            resultSet.getString(5)+"',"+doy()+",'"+resultSet.getString(7)+"','"+resultSet.getString(8)+"','"+
                            resultSet.getString(9)+"','"+resultSet.getString(10)+"','"+resultSet.getString(11)+"','"+resultSet.getString(12)+"','"+
                            resultSet.getString(13)+"','"+resultSet.getString(14)+"',"+resultSet.getString(15)+","+comId.toString()+",1,'"+code+"')")
                    val oldId=resultSet.getString(16)
                    sqlite2?.update("UPDATE customer set erpupd=1 where id="+oldId)
                    val newId=sqlsrv1?.queryForObject<String>("select id from customer where code='"+code+"' and comId="+comId)
                    sqlite2?.update("UPDATE customer set erpid="+newId+" where id="+oldId)
                    sqlite2?.update("UPDATE customer set id="+newId+" where id="+oldId)
                    sqlite2?.update("UPDATE cashtrn set pererpid="+newId+" where perid="+oldId)
                    sqlite2?.update("UPDATE fintrade set cuserpid="+newId+" where cusid="+oldId)


                }while (resultSet.next())
            }


        println("CUSTOMER NEW")
        /////////////////////////??CUSTOMER CHANGE
        sqlite1?.query("SELECT name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city,comments,routeid,id from customer where erpupd=2")
        { resultSet ->
            do {
                println("UPDATE")
                sqlsrv1?.update("UPDATE CUSTOMER SET name='" + resultSet.getString(1) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET street1='" + resultSet.getString(2) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET district1='" + resultSet.getString(3) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET identitynum='" + resultSet.getString(4) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET afm='" + resultSet.getString(5) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET doyid=" + resultSet.getString(6) + " where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET occupation='" + resultSet.getString(7) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET phone11='" + resultSet.getString(8) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET phone12='" + resultSet.getString(9) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET fax1='" + resultSet.getString(10) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET email='" + resultSet.getString(11) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET fpastatus=" + resultSet.getString(12) + " where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET city1='" + resultSet.getString(13) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMER SET remarks='" + resultSet.getString(14) + "' where id=" + resultSet.getString(16))

                sqlite2?.update("UPDATE customer set erpupd=1 where id=" + resultSet.getString(16)
                )


            } while (resultSet.next())
        }

                println("CUSTOMER CHANGE")


        /// SALES INVOICES

        sqlsrv1?.update("DELETE from z_fintrade")
        sqlsrv1?.update("DELETE from z_storetradelines")
        sqlsrv1?.update("DELETE from z_cash")
        var dsrId:Int?=0
        var ftrId:Int?=0




        sqlite2?.query("SELECT ftrdate,f.dsrid,f.dsrnumber,f.cuserpid,f.comments,f.deliveryaddress,f.netvalue,f.vatamount,f.totamount,f.id," +
                "c.vatstatusid,f.suberpid,f.shptoperid,f.shptoaddid from fintrade f,customer c where c.id=f.cusid and f.erpupd=0")
            {   resultSet->
                    do  {

                        val stdvatstatus=resultSet.getInt(11)
                        val tdate=resultSet.getString(1)


                        when(resultSet.getInt(2))
                        {

                            1->dsrId=sqlsrv2?.queryForObject<Int>("SELECT tdadsrid from z_pda where salesmanid="+salesmanid)
                            2->dsrId=sqlsrv2?.queryForObject<Int>("SELECT pisdsrid from z_pda where salesmanid="+salesmanid)
                            3->dsrId=sqlsrv2?.queryForObject<Int>("SELECT daddsrid from z_pda where salesmanid="+salesmanid)
                            4->dsrId=sqlsrv2?.queryForObject<Int>("SELECT depdsrid from z_pda where salesmanid="+salesmanid)
                            5->dsrId=sqlsrv2?.queryForObject<Int>("SELECT pagdsrid from z_pda where salesmanid="+salesmanid)
                            6->dsrId=sqlsrv2?.queryForObject<Int>("SELECT ddedsrid from z_pda where salesmanid="+salesmanid)
                            7->dsrId=sqlsrv2?.queryForObject<Int>("SELECT sdadsrid from z_pda where salesmanid="+salesmanid)
                        }



                        sqlsrv1?.update("INSERT INTO z_fintrade (ftrdate,dsrid,dsrnumber,cusid,comments,deliveryaddress,netvalue,vatamount,totamount,salesmanid,vatstatusid,f.suberpid,f.shptoperid,f.shptoaddid) values ('"
                                +tdate+"',"+dsrId.toString()+","+ resultSet.getInt(3).toString()+","+resultSet.getInt(4).toString()+",'"+ resultSet.getString(5)+"','"+resultSet.getString(6)
                                +"',"+resultSet.getFloat(7).toString()+","+resultSet.getFloat(8).toString()+","+resultSet.getFloat(9).toString()+","+salesmanid+","+stdvatstatus+
                                ","+resultSet.getInt(12)+","+resultSet.getInt(13)+","+resultSet.getInt(14)+")")
                        val t1="SELECT id from z_fintrade where ftrdate='"+tdate+"' and dsrid="+dsrId.toString()+" and dsrnumber="+resultSet.getInt(3).toString()+
                                " and cusid="+resultSet.getInt(4).toString()
                        println (t1)
                        ftrId=sqlsrv1?.queryForObject<Int>(t1)

                        sqlite2?.query("SELECT iteid,primaryqty,price,discount,secdiscount,linevalue,vatamount,vatid from storetradelines where ftrid="+resultSet.getInt(10).toString())
                        {
                            resultSet1->
                                do{

                                    var vtcId = when (stdvatstatus == 1) {
                                        true -> 99
                                        false -> resultSet1.getInt(8)
                                    }
                                    sqlsrv1?.update("INSERT INTO z_storetradelines(ftrid,iteid,primaryqty,price,discount,secdiscount,linevalue,vatamount,vatid) values (" + ftrId + "," + resultSet1.getInt(1).toString()
                                            + "," + resultSet1.getFloat(2).toString() + "," + resultSet1.getFloat(3).toString() + "," + resultSet1.getFloat(4).toString() + ","
                                            + resultSet1.getFloat(5).toString() + "," + resultSet1.getFloat(6).toString() + "," + resultSet1.getFloat(7).toString() + "," + vtcId.toString() + ")")
                                }while (resultSet1.next())
                            }


                        sqlite2?.query("SELECT trndate,perid,justification,amount from cashtrn where trntype=1 and ftrid="+resultSet.getInt(10).toString())
                        { resultSet2->
                                    do {
                                        val dsrid=sqlsrv1?.queryForObject<Int>("SELECT metdsrid from z_pda where salesmanid="+salesmanid)
                                        val tdate=resultSet.getDate(1).toString()
                                        sqlsrv1?.update("INSERT INTO z_cash (cusid,amount,trndate,salesmanid,dsrid,ftrid,justification) values ("+resultSet2.getInt(2).toString()+","+
                                                resultSet2.getFloat(4).toString()+",'"+tdate+"',"+salesmanid+","+dsrid+","+ftrId+",'"+resultSet2.getString(3)+"')")
                                    }while (resultSet2.next())
                                }



                        }while (resultSet.next())
                }



        println("SALES INVOICES")

        ///////////////////////////////////COLLECTIONS


        sqlite1?.query("SELECT trndate,pererpid,justification,amount from cashtrn where trntype=1 and ftrid is null")
             { resultSet->
                do {
                    val dsrid=sqlsrv1?.queryForObject<Int>("SELECT metdsrid from z_pda where salesmanid="+salesmanid)
                    val tdate=resultSet.getDate(1).toString()
                    sqlsrv1?.update("INSERT INTO z_cash(trndate,cusid,justification,amount,salesmanid,dsrid) values('"+tdate+"',"+resultSet.getInt(1).toString()+
                            ",'"+resultSet.getString(3)+"',"+resultSet.getFloat(4.toString())+","+salesmanid+","+dsrid+")")
                }while (resultSet.next())
            }



        println("COLLECTIONS")
        ////////////// LOG

       // sqlsrv1?.update("INSERT INTO z_log (task,backupfile,cdatetime) values (2,'algo.sqlite."+LocalDateTime.now().toString()+"-2',getdate())")


        ///
        sqlite2?.update("DELETE from fintrade ")
        sqlite2?.update("DELETE from storetradelines ")
        sqlite2?.update("DELETE from cashtrn ")



    }


    override fun AtlantistoAndroid() {




        sqlite2?.update("VACUUM")



        /////MATERIAL
        sqlite2?.update("DELETE from material")
        sqlsrv1?.query("SELECT m.code,m.description,m.whsprice,m.vtcid,m.id,m.defaultdiscount,ms.code from $tablePrefix.material m,$tablePrefix.mesunit ms where m.mu1=ms.codeid and m.isactive=1 and m.code like '$materialMask%'and m.comId=$comId"){
            resultSet->
                sqlite2?.update("BEGIN")
                            do {
                    sqlite2?.update("INSERT INTO material (code,description,price,vatid,erpid,maxdiscount,unit) VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)+"',"+resultSet.getFloat(3).toString()+","+resultSet.getInt(4).toString()
                            +","+resultSet.getInt(5).toString()+","+resultSet.getFloat(6).toString()+",'"+resultSet.getString(7).toString()+"')")
                }while (resultSet.next())
            sqlite2?.update("END")

            }


        /////VAT
        sqlite2?.update("DELETE from vat")


        sqlsrv1?.query("SELECT v.codeid,v.percentage,vs.vtsid from $tablePrefix.vatcategory v,$tablePrefix.vatstatus vs where vs.vtnid=v.codeid")
        {resultSet->
            sqlite2?.update("BEGIN")
                do {
                    sqlite2?.update("INSERT INTO vat (codeid,percent0,percent1) VALUES ('"+resultSet.getString(1)+"',"+resultSet.getFloat(2)+","+resultSet.getFloat(3).toString()+")")
                } while (resultSet.next())
            sqlite2?.update("END")

            }




        /// CUSTOMER

        sqlite2?.update("DELETE from customer")

        //TODO()  Custom KAVOUKIS
        //sqlsrv1?.query("SELECT id,name,street1,district1,identitynum,afm,doyid,occupation,phone11,phone12,fax1,email,fpastatus, city1,remarks,rotid,z_isthird from customer where isactive=1 and comId="+comId)
        sqlsrv1?.query("SELECT c.id,name,c.street1,c.district1,c.identitynum,afm,doyid,occupation,phone11,phone12,fax1,email,fpastatus, city1,cast(remarks as varchar(100)),rc.rotid,z_isthird " +
                "from $tablePrefix.customer c inner join $tablePrefix.mobileroutecustomer rc on rc.cusid=c.id inner join $tablePrefix.mobileroute r on rc.rotid=r.id where r.colidsalesman=$salesmanid UNION "+
                "SELECT c.id,c.name,c.street1,c.district1,identitynum,afm,doyid,occupation,phone11,phone12,fax1,email,fpastatus, city1,cast(remarks as varchar(100)),rc.rotid,z_isthird from $tablePrefix.customer c inner join "+
                        "$tablePrefix.mobileroutecustomer rc on rc.shptoperid=c.id inner join $tablePrefix.mobileroute r on rc.rotid=r.id where r.colidsalesman=$salesmanid")
                {resultSet->
                    sqlite2?.update("BEGIN")
                do{
                    sqlite2?.update("INSERT INTO customer (erpid,name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city,comments,routeid,erpupd,isthird) VALUES ("+resultSet.getInt(1).toString()+
                            ",'" +resultSet.getString(2)+"','"+resultSet.getString(3) +"','"+resultSet.getString(4)+"','"+resultSet.getString(5)+"','"+resultSet.getString(6)+
                            "',"+resultSet.getInt(7).toString()+",'"+resultSet.getString(8)+"','"+resultSet.getString(9)+"','"+resultSet.getString(10)+"','"+resultSet.getString(11)+
                            "','"+resultSet.getString(12)+"',"+resultSet.getInt(13).toInt()+",'"+resultSet.getString(14)+"','"+resultSet.getString(15)+"',"+resultSet.getInt(16).toString()+",1,"+resultSet.getInt(17)+")"                    )
                } while (resultSet.next())
                    sqlite2?.update("END")

            }


        ////CUSTFINDATA

        sqlite2?.update("DELETE from custfindata")


        sqlsrv1?.query("SELECT masterid,masterbalance from $tablePrefix.custfindata"){
            resultSet->
            sqlite2?.update("BEGIN")
                do {
                    sqlite2?.update("INSERT INTO custfindata (cusid,balance) VALUES ("+resultSet.getInt(1).toString()+"," +resultSet.getFloat(2).toString()+")")
                } while (resultSet.next())
            sqlite2?.update("END")

            }






        ///DOY

        sqlite2?.update("DELETE from doy")


        sqlsrv1?.query("SELECT codeid,replace(descr,'''',''),code from $tablePrefix.doy"){
            resultSet->
            sqlite2?.update("BEGIN")
                do {
                    sqlite2?.update("INSERT INTO doy(erpid,description,code) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"','"+resultSet.getString(3)+"')")
                }  while (resultSet.next())
            sqlite2?.update("END")
            }

        ///ROUTE



        sqlite2?.update("DELETE from route")

        //TODO() CUSTOM KAVOUKIS
        //sqlsrv1?.query("SELECT codeid,descr from route where comId="+comId)
        sqlsrv1?.query("SELECT id,descr from $tablePrefix.mobileroute where colidsalesman=$salesmanid")
        {
            resultSet->
            sqlite2?.update("BEGIN")
                do {
                    sqlite2?.update("INSERT INTO route(erpid,description) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
                }  while (resultSet.next())
            sqlite2?.update("END")

            }


        ///PRDATA

        sqlite2?.update("DELETE from customerprices")


        sqlsrv1?.query(pricesQuery){
            resultSet->
            sqlite2?.update("BEGIN")
                do {
                    sqlite2?.update("INSERT INTO customerprices (cusid,iteid,price,discount,secdiscount) VALUES ("+resultSet.getInt(1).toString()+"," +resultSet.getInt(2).toString()
                            +","+resultSet.getFloat(3).toString()+","+resultSet.getFloat(4).toString()+","+resultSet.getFloat(5).toString()+")")
                } while (resultSet.next())
            sqlite2?.update("END")

            }





        ///SALESMAN

        sqlite2?.update("DELETE from salesman")


        sqlsrv1?.query("SELECT id,name from $tablePrefix.salesman where comId="+comId){
            resultSet->
            sqlite2?.update("BEGIN")
                do {
                    sqlite2?.update("INSERT INTO salesman (erpid,name) values ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
                }  while (resultSet.next())
            sqlite2?.update("END")

            }



        ///SUBSIDIARIES

        sqlite2?.update("DELETE from subsidiary")

//TODO() CUSTOM KAVOUKIS
        //sqlsrv1?.query("SELECT cu.descr,cu.street,cu.district,cu.fpastatus,cu.rotid,cu.perid,cu.city from custaddress cu inner join customer c on" +
          //      "cu.perid=c.id where c.comId="+comId)
        sqlsrv1?.query("select cu.descr,cu.street,cu.district,cu.fpastatus,cu.rotid,cu.perid,cu.id,cu.city from $tablePrefix.customer c " +
                "inner join $tablePrefix.custaddress cu on c.id=cu.perid where " +
                "((exists (select * from $tablePrefix.mobileroutecustomer rc  inner join $tablePrefix.mobileroute r on rc.rotid=r.id" +
                " and rc.shptoaddid=cu.id  and r.colidsalesman=$salesmanid)) or (exists(" +
                "select * from $tablePrefix.mobileroutecustomer rc  inner join $tablePrefix.mobileroute r on rc.rotid=r.id" +
                " and rc.addid=cu.id  and r.colidsalesman=$salesmanid)))")
        {
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO subsidiary (descr,street,district,vatstatus,rotid,perid,erpid,city) values ('"+
                        resultSet.getString(1)+"','"+resultSet.getString(2)+"','"+resultSet.getString(3)+"',"+
                        resultSet.getInt(4).toString()+","+resultSet.getInt(5)+","+resultSet.getInt(6) +
                        ","+resultSet.getInt(7)+",'"+resultSet.getString(8)+"')")
            }  while (resultSet.next())
            sqlite2?.update("END")

        }




        /////TABLETINFO


        sqlite2?.update("DELETE from salesman")


        sqlsrv1?.query("SELECT salesmanid,printermac from $tablePrefix.z_pda where salesmanid="+salesmanid){
            resultSet->
            sqlite2?.update("BEGIN")
                do {
                    sqlite2?.update("INSERT INTO tabletinfo (salesmanid,printercode) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
                }  while (resultSet.next())
            sqlite2?.update("END")

            }



        //////STOREFINDATA


        sqlite2?.update("DELETE from storefindata")
        val sdadsrid= sqlsrv1?.queryForObject<Int>("SELECT sdadsrid from $tablePrefix.z_pda where salesmanid="+salesmanid)

        val ftrid=sqlsrv1?.queryForObject<Int>("select max(id) from fintrade f where f.dsrid="+sdadsrid.toString()+" and f.ftrdate=DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0)  and f.comId="+comId)

        val qry1="SELECT m.id,0 from $tablePrefix.material m where m.isactive=1 and m.code like '$materialMask%'and m.comId=$comId"
        //TODO() CUSTOM KAVOUKIS
        //val qry1="select st.iteid,sum(st.primaryqty) from $tablePrefix.fintrade f,$tablePrefix.storetradelines st where st.ftrid=f.id and f.id="+ftrid+" group by st.iteid"
        sqlsrv1?.query(qry1){
            resultSet->
            sqlite2?.update("BEGIN")
                do {
                    val t="INSERT INTO storefindata (iteid,startqty,qty) VALUES ("+resultSet.getInt(1)+","+resultSet.getFloat(2).toString()+","+resultSet.getFloat(2).toString()+
                    ")"
                    println(t)
                    sqlite2?.update(t)

                }  while (resultSet.next())
            sqlite2?.update("END")
            }

        /////STORECUSTDATA
        sqlite2?.update("DELETE from storecustdata")
        val query1="with cte as (select c.id cusid,m.id iteid,f.ftrdate ftrdate,st.primaryqty primaryqty,st.PRICE price,st.PRCDISC1 discount,st.PRCDISC2 discount2,ROW_NUMBER() " +
                "over (partition by c.id,m.id order by f.ftrdate desc) as rn from $tablePrefix.customer c inner join $tablePrefix.FINTRADE f on c.id=f.CUSID inner join $tablePrefix.STORETRADELINES st on st.FTRID=f.id " +
                "inner join $tablePrefix.ITEMTRANS i on i.stlid=st.ID inner join $tablePrefix.MATERIAL m on m.id=i.ITEID  where m.comId=$comId and i.OUTPUTVALMODE=1) select * from cte where rn=1"

        sqlsrv1?.query(query1){
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO storecustdata (iteid,cusid,lastqty,lastprice,lastdiscount,lastdiscount2,lastdate) values("+
                        resultSet.getInt(2).toString()+","+resultSet.getInt(1).toString()+
                        ","+resultSet.getFloat(4).toString()+","+resultSet.getFloat(5).toString()+
                        ","+resultSet.getFloat(6).toString()+","+resultSet.getFloat(7).toString()+
                        ",'"+resultSet.getString(3)+"')")

            }  while (resultSet.next())
            sqlite2?.update("END")

        }




        /////////DOCSERIES



        var codeId:Int=0
        var series:String=""
        var lastno:Int?=0
        var copies:Int=0

        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series,numofcopies from $tablePrefix.docseries d,$tablePrefix.z_pda z where d.codeid=z.tdadsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comId="+comId)
            { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
                series=rs.getString("series")
                copies=rs.getInt("numofcopies")

            }


        sqlite2?.update("UPDATE docseries set erpdsrid=$codeId,copies=$copies where id=1")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=1")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from $tablePrefix.fintrade where fyeid=year(getdate()) and domaintype=2 and comId="+comId+" and dsrid="+codeId)

        val qry="UPDATE docseries set lastno="+lastno.toString()+" where id=1"

        sqlite2?.update(qry)







        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series,numofcopies from $tablePrefix.docseries d,$tablePrefix.z_pda z where d.codeid=z.pisdsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comId="+comId)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            copies=rs.getInt("numofcopies")


        }


        sqlite2?.update("UPDATE docseries set erpdsrid=$codeId,copies=$copies where id=2")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=2")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from $tablePrefix.fintrade where fyeid=year(getdate()) and domaintype=2 and comId="+comId+" and dsrid="+codeId)

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=2")



        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series,numofcopies from $tablePrefix.docseries d,$tablePrefix.z_pda z where d.codeid=z.dadsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comId="+comId)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            copies=rs.getInt("numofcopies")


        }


        sqlite2?.update("UPDATE docseries set erpdsrid=$codeId,copies=$copies where id=3")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=3")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from $tablePrefix.fintrade where fyeid=year(getdate()) and domaintype=2 and comId="+comId+" and dsrid="+codeId)

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=3")



        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series,numofcopies from $tablePrefix.docseries d,$tablePrefix.z_pda z where d.codeid=z.depdsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comId="+comId)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            copies=rs.getInt("numofcopies")

        }


        sqlite2?.update("UPDATE docseries set erpdsrid=$codeId,copies=$copies where id=4")
        // sqlite1?.update("UPDATE docseries set series='"+series+"'where id=4")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from $tablePrefix.fintrade where fyeid=year(getdate()) and domaintype=2 and comId="+comId+" and dsrid="+codeId)

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=4")


        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series,numofcopies from $tablePrefix.docseries d,$tablePrefix.z_pda z where d.codeid=z.pagdsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comId="+comId)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            copies=rs.getInt("numofcopies")

        }


        sqlite2?.update("UPDATE docseries set erpdsrid=$codeId,copies=$copies where id=5")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=5")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from $tablePrefix.fintrade where fyeid=year(getdate()) and domaintype=2 and comId="+comId+" and dsrid="+codeId)

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=5")



        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series,numofcopies from $tablePrefix.docseries d,$tablePrefix.z_pda z where d.codeid=z.ddedsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comId="+comId)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            copies=rs.getInt("numofcopies")

        }


        sqlite2?.update("UPDATE docseries set erpdsrid=$codeId,copies=$copies where id=6")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=5")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from $tablePrefix.fintrade where fyeid=year(getdate()) and domaintype=2 and comId="+comId+" and dsrid="+codeId)

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=6")



        sqlsrv1?.queryForObject("SELECT codeid,left(code,2) as series,numofcopies from $tablePrefix.docseries d,$tablePrefix.z_pda z where d.codeid=z.sdadsrid and z.salesmanid="+salesmanid+" and d.domaintype=2 and d.comId="+comId)
        { rs: ResultSet, _: Int -> codeId=rs.getInt("codeid")
            series=rs.getString("series")
            copies=rs.getInt("numofcopies")

        }


        sqlite2?.update("UPDATE docseries set erpdsrid=$codeId,copies=$copies where id=7")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=1")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnumber),0) from $tablePrefix.fintrade where fyeid=year(getdate()) and domaintype=2 and comId="+comId+" and dsrid="+codeId)

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=7")









        ///COMPANYDATA


        sqlite2?.update("DELETE from companydata")


        sqlsrv1?.query("select top 1 name,occupation,address,city,afm,doy,tel1,tel2,email,site from $tablePrefix.z_companydata"){
            resultSet->

                    sqlite2?.update("INSERT INTO companydata (name,occupation,address,city,afm,doy,tel1,tel2,email,site) VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)
                            +"','"+resultSet.getString(3)+"','"+resultSet.getString(4)+"','"+resultSet.getString(5)+"','"+resultSet.getString(6)+"','"+
                            resultSet.getString(7)+"','"+resultSet.getString(8)+"','"+resultSet.getString(9)+"','"+resultSet.getString(10)+"')")
                }



        println("COMANYDATA")

        //sqlsrv1?.update("INSERT INTO z_log (task,backupfile,cdatetime) values (1,'algo.sqlite.db"+LocalDateTime.now().toString()+"-1',getdate())")

        closeConnections()
    }


    fun closeConnections()
    {
        sqlite1?.dataSource?.connection?.close()
        sqlite2?.dataSource?.connection?.close()
        sqlite3?.dataSource?.connection?.close()
        sqlsrv1?.dataSource?.connection?.close()
        sqlsrv2?.dataSource?.connection?.close()

    }






    override fun AndroidtoCapital() {



        sqlite1?.query("SELECT name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city," +
                " comments,5,id from customer where erpid=0")
        {resultSet->
            do

            {

                val code:String?=(Integer.parseInt(sqlsrv1?.queryForObject("SELECT TOP 1 CODE FROM customers where code like '$codeMask%' order by code desc", String::class.java))+1).toString().padStart(4,'0')
                val doy={if(resultSet.getInt(6)==0) "NULL" else resultSet.getInt(6).toString()}
                sqlsrv1?.update("INSERT INTO CUSTOMERS(descr,address,district,userstr1,tin,tcdid,occupation,phone,phone2,fax,email,vstid,city,comment,rotid,code) " +
                        "VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)+"','"+resultSet.getString(3)+"','"+resultSet.getString(4)+"','"+
                        resultSet.getString(5)+"',"+doy()+",'"+resultSet.getString(7)+"','"+resultSet.getString(8)+"','"+
                        resultSet.getString(9)+"','"+resultSet.getString(10)+"','"+resultSet.getString(11)+"',"+if(resultSet.getInt(12)==1)  '1' else '0'+"','"+
                        resultSet.getString(13)+"','"+resultSet.getString(14)+"',"+resultSet.getString(15)+",'"+code+"')")
                val oldId=resultSet.getString(16)
                sqlite2?.update("UPDATE customer set erpupd=1 where id="+oldId)
                val newId=sqlsrv1?.queryForObject<String>("select id from customers where code='"+code+"'")
                sqlite2?.update("UPDATE customer set erpid="+newId+" where id="+oldId)
                sqlite2?.update("UPDATE customer set id="+newId+" where id="+oldId)
                sqlite2?.update("UPDATE cashtrn set pererpid="+newId+" where perid="+oldId)
                sqlite2?.update("UPDATE fintrade set cuserpid="+newId+" where cusid="+oldId)


            }while (resultSet.next())
        }


        println("CUSTOMER NEW")
        /////////////////////////??CUSTOMER CHANGE
        sqlite1?.query("SELECT name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city,comments,routeid,id from customer where erpupd=2")
        { resultSet ->
            do {
                println("UPDATE")
                sqlsrv1?.update("UPDATE CUSTOMERS SET descr='" + resultSet.getString(1) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET address='" + resultSet.getString(2) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET district='" + resultSet.getString(3) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET userstr1='" + resultSet.getString(4) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET tin='" + resultSet.getString(5) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET tcdid=" + resultSet.getString(6) + " where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET occupation='" + resultSet.getString(7) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET phone='" + resultSet.getString(8) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET phone2='" + resultSet.getString(9) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET fax='" + resultSet.getString(10) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET email='" + resultSet.getString(11) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET vstid=" + if(resultSet.getInt(12)==1) "1" else "0" + " where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET city='" + resultSet.getString(13) + "' where id=" + resultSet.getString(16))
                sqlsrv1?.update("UPDATE CUSTOMERS SET comment='" + resultSet.getString(14) + "' where id=" + resultSet.getString(16))

                sqlite2?.update("UPDATE customer set erpupd=1 where id=" + resultSet.getString(16)
                )


            } while (resultSet.next())
        }

        println("CUSTOMER CHANGE")


        /// SALES INVOICES

        ////*******************TODO() SECOND DISCOUNT

        sqlsrv1?.update("DELETE from z_fintrade")
        sqlsrv1?.update("DELETE from z_storetradelines")
        sqlsrv1?.update("DELETE from z_cash")
        var dsrId:Int?=0
        var ftrId:Int?=0




        sqlite2?.query("SELECT ftrdate,f.dsrid,f.dsrnumber,f.cuserpid,f.comments,f.deliveryaddress,f.netvalue,f.vatamount,f.totamount,f.id," +
                "c.vatstatusid,f.suberpid,f.shptoperid,f.shptoaddid from fintrade f,customer c where c.erpid=f.cusid and f.erpupd=0")
        {   resultSet->

            do  {

                val stdvatstatus=resultSet.getInt(11)
                val tdate=resultSet.getString(1)


                when(resultSet.getInt(2))
                {
                    1->dsrId=sqlsrv2?.queryForObject<Int>("SELECT tdadsrid from z_pda where salesmanid="+salesmanid)
                    2->dsrId=sqlsrv2?.queryForObject<Int>("SELECT pisdsrid from z_pda where salesmanid="+salesmanid)
                    3->dsrId=sqlsrv2?.queryForObject<Int>("SELECT daddsrid from z_pda where salesmanid="+salesmanid)
                    4->dsrId=sqlsrv2?.queryForObject<Int>("SELECT depdsrid from z_pda where salesmanid="+salesmanid)
                    5->dsrId=sqlsrv2?.queryForObject<Int>("SELECT pagdsrid from z_pda where salesmanid="+salesmanid)
                    6->dsrId=sqlsrv2?.queryForObject<Int>("SELECT ddedsrid from z_pda where salesmanid="+salesmanid)
                    5->dsrId=sqlsrv2?.queryForObject<Int>("SELECT sdadsrid from z_pda where salesmanid="+salesmanid)
                }

                sqlsrv1?.update("INSERT INTO z_fintrade (ftrdate,dsrid,dsrnumber,cusid,comments,deliveryaddress,netvalue,vatamount,totamount,salesmanid,vatstatusid,suberpid,shptoperid,shptoaddid) values ('"
                        +tdate+"',"+dsrId.toString()+","+ resultSet.getInt(3).toString()+","+resultSet.getInt(4).toString()+",'"+ resultSet.getString(5)+"','"+resultSet.getString(6)
                        +"',"+resultSet.getFloat(7).toString()+","+resultSet.getFloat(8).toString()+","+resultSet.getFloat(9).toString()+","+salesmanid+","+stdvatstatus+
                        ","+resultSet.getInt(12)+","+resultSet.getInt(13)+","+resultSet.getInt(14)+")")
                ftrId=sqlsrv1?.queryForObject<Int>("SELECT id from z_fintrade where ftrdate='"+tdate+"' and dsrid="+dsrId.toString()+" and dsrnumber="+resultSet.getInt(3).toString()+
                        " and cusid="+resultSet.getInt(4).toString())

                sqlite2?.query("SELECT iteid,primaryqty,price,discount,secdiscount,linevalue,vatamount,vatid from storetradelines where ftrid="+resultSet.getInt(10).toString())
                {
                    resultSet1->
                    do{
                        var vtcId = when (stdvatstatus == 1) {
                            true -> 99
                            false -> resultSet1.getInt(8)
                        }
                        sqlsrv1?.update("INSERT INTO z_storetradelines(ftrid,iteid,primaryqty,price,discount,secdiscount,linevalue,vatamount,vatid) values (" + ftrId + "," + resultSet1.getInt(1).toString()
                                + "," + resultSet1.getFloat(2).toString() + "," + resultSet1.getFloat(3).toString() + "," + resultSet1.getFloat(4).toString() + ","
                                + resultSet1.getFloat(5).toString() + "," + resultSet1.getFloat(6).toString() + "," + resultSet1.getFloat(7).toString() + "," + vtcId.toString() + ")")
                    }while (resultSet1.next())
                }


                sqlite2?.query("SELECT trndate,perid,justification,amount from cashtrn where trntype=1 and ftrid="+resultSet.getInt(10).toString())
                { resultSet2->
                    do {
                        val dsrid=sqlite2?.queryForObject<Int>("SELECT metdsrid from z_pda where salesmanid="+salesmanid)
                        val tdate=resultSet.getDate(1).toString()
                        sqlsrv1?.update("INSERT INTO z_cash (cusid,amount,trndate,salesmanid,dsrid,ftrid,justification) values ("+resultSet2.getInt(2).toString()+","+
                                resultSet2.getFloat(4).toString()+",'"+tdate+"',"+salesmanid+","+dsrid+","+ftrId+",'"+resultSet2.getString(3)+"')")
                    }while (resultSet2.next())
                }



            }while (resultSet.next())
        }

            sqlsrv1?.update("update z_fintrade set cuscode=(select code from customers where id=z_fintrade.cusid)")
            sqlsrv1?.update("update z_storetradelines set itecode=(select code from stockitems where id=z_storetradelines.iteid)")


        println("SALES INVOICES")

        ///////////////////////////////////COLLECTIONS


        sqlite1?.query("SELECT trndate,pererpid,justification,amount from cashtrn where trntype=1 and ftrid is null")
        { resultSet->
            do {
                val dsrid=sqlsrv1?.queryForObject<Int>("SELECT metdsrid from z_pda where salesmanid="+salesmanid)
                val tdate=resultSet.getString(1)
                sqlsrv1?.update("INSERT INTO z_cash(trndate,cusid,justification,amount,salesmanid,dsrid) values('"+tdate+"',"+resultSet.getInt(1).toString()+
                        ",'"+resultSet.getString(3)+"',"+resultSet.getFloat(4).toString()+","+salesmanid+","+dsrid+")")
            }while (resultSet.next())
        }



        println("COLLECTIONS")
        ////////////// LOG

        sqlsrv1?.update("INSERT INTO z_log (task,backupfile,cdatetime) values (2,'algo.sqlite."+LocalDateTime.now().toString()+"-2',getdate())")

        println("Z_LOG")
        ///
        sqlite2?.update("DELETE from fintrade ")
        sqlite2?.update("DELETE from storetradelines ")
        sqlite2?.update("DELETE from cashtrn ")



    }








    override fun CapitaltoAndroid() {


        sqlite2?.update("VACUUM")



        /////MATERIAL
        //TODO("ISACTIVE ITEMS FIELD?????")
        sqlite2?.update("DELETE from material")

        sqlsrv1?.query("SELECT m.code,m.descr,m.whsprice,m.vatid,m.id,m.discount,ms.descr from stockitems m,stockunits ms where m.stuid=ms.idx and m.code not like '99%'"){
            resultSet->
            sqlite2?.update("BEGIN")
            
            do {
                sqlite2?.update("INSERT INTO material (code,description,price,vatid,erpid,maxdiscount,unit) VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)+"',"+resultSet.getFloat(3).toString()+","+resultSet.getInt(4).toString()
                        +","+resultSet.getInt(5).toString()+","+resultSet.getFloat(6).toString()+",'"+resultSet.getString(7).toString()+"')")
            }while (resultSet.next())
            sqlite2?.update("END")

        }
        println("MATERIAL")

        /////VAT
        sqlite2?.update("DELETE from vat")


        sqlsrv1?.query("select v.IDX,v.APERCENT,(select APERCENT from vat v1 where v1.idx=(select vs.VATID2 from VATSTATUSLINES vs where vs.VATID=v.IDX and vs.MASTERID=(select id from VATSTATUS vts where vts.IDX=1))) from vat v")
        {resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO vat (codeid,percent0,percent1) VALUES ('"+resultSet.getString(1)+"',"+resultSet.getFloat(2)+","+resultSet.getFloat(3).toString()+")")
            } while (resultSet.next())
            sqlite2?.update("END")

        }

        println("VAT")


        /// CUSTOMER

        sqlite2?.update("DELETE from customer")


        sqlsrv1?.query("select id,DESCR,ADDRESS,DISTRICT,USERSTR1 as title,tin as afm,TCDID as doyid,OCCUPATION,phone,phone2,replace(FAX,'''',' '),EMAIL,case vstid when 1 then 1 else 0 end,city,replace(COMMENT,'''',' '),ROTID,z_isthird from customers where code not like '99%'")
        {resultSet->
            sqlite2?.update("BEGIN")
            do{

                sqlite2?.update("INSERT INTO customer (erpid,name,address,district,title,afm,doyid,occupation,tel1,tel2,fax,email,vatstatusid,city,comments,routeid,erpupd,isthird) VALUES ("+resultSet.getInt(1).toString()+
                        ",'" +resultSet.getString(2)+"','"+resultSet.getString(3) +"','"+resultSet.getString(4)+"','"+resultSet.getString(5)+"','"+resultSet.getString(6)+
                        "',"+resultSet.getInt(7).toString()+",'"+resultSet.getString(8)+"','"+resultSet.getString(9)+"','"+resultSet.getString(10)+"','"+resultSet.getString(11)+
                        "','"+resultSet.getString(12)+"',"+resultSet.getInt(13).toString()+",'"+resultSet.getString(14)+"','"+resultSet.getString(15)+"',"+resultSet.getInt(16).toString()+","+resultSet.getInt(17)+",1)"                    )
            } while (resultSet.next())
            sqlite2?.update("END")

        }

        println("CUSTOMER")
        ////CUSTFINDATA

        sqlite2?.update("DELETE from custfindata")


        sqlsrv1?.query("SELECT cusid,balance from customerfindata"){
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO custfindata (cusid,balance) VALUES ("+resultSet.getInt(1).toString()+"," +resultSet.getFloat(2).toString()+")")
            } while (resultSet.next())
            sqlite2?.update("END")

        }




        println("CUSTFINDATA")

        ///DOY

        sqlite2?.update("DELETE from doy")


        sqlsrv1?.query("SELECT idx,replace(descr,'''',' '),code from taxoffices"){
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO doy(erpid,description,code) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"','"+resultSet.getString(3)+"')")
            }  while (resultSet.next())
            sqlite2?.update("END")

        }

        println("DOY")

        ///ROUTE



        sqlite2?.update("DELETE from route")


        sqlsrv1?.query("SELECT idx,descr from salesroutes")
        {
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO route(erpid,description) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
            }  while (resultSet.next())
            sqlite2?.update("END")

        }
        println("ROUTE")

        ///PRDATA
        //TODO()********************CUSTOMER PRICES
        /*
        sqlite2?.update("DELETE from customerprices")


        sqlsrv1?.query("SELECT domainid1,domainid2,fld1 from prdata where comId="+comId){
            resultSet->
            do {
                sqlite2?.update("INSERT INTO customerprices (cusid,iteid,price) VALUES ("+resultSet.getInt(1).toString()+"," +resultSet.getString(2)+","+resultSet.getFloat(3).toString()+")")
            } while (resultSet.next())

        }


        println("PRDATA")

         */





        ///SALESMAN

        sqlite2?.update("DELETE from salesman")


        sqlsrv1?.query("SELECT idx,descr from persons"){
            resultSet->
            sqlite2?.update("BEGIN")


            do {
                sqlite2?.update("INSERT INTO salesman (erpid,name) values ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
            }  while (resultSet.next())
            sqlite2?.update("END")

        }
        println("SALESMAN")

        ///SUBSIDIARIES

        sqlite2?.update("DELETE from subsidiary")


        sqlsrv1?.query("SELECT cu.descr,cu.address,cu.district,cu.vstid,cu.rotid,cu.cusid,cu.city from CUSTOMERBRANCHES cu"){
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO subsidiary (descr,street,district,vatstatus,rotid,perid,erpid,city) values ('"+
                        resultSet.getString(1)+"','"+resultSet.getString(2)+"','"+resultSet.getString(3)+"',"+
                        resultSet.getInt(4).toString()+","+resultSet.getInt(5)+","+resultSet.getInt(6) +
                        ","+resultSet.getInt(7)+",'"+resultSet.getString(8)+"')")
            }  while (resultSet.next())
            sqlite2?.update("END")

        }

        println("SUBSIDIARIES")


        /////TABLETINFO


        sqlite2?.update("DELETE from salesman")


        sqlsrv1?.query("SELECT salesmanid,printermac from z_pda where salesmanid="+salesmanid){
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO tabletinfo (salesmanid,printercode) VALUES ("+resultSet.getInt(1).toString()+",'" +resultSet.getString(2)+"')")
            }  while (resultSet.next())
            sqlite2?.update("END")

        }


        println("TABLETINFO")
        //////STOREFINDATA


        sqlite2?.update("DELETE from storefindata")
        // CUSTOM LUI
        // val sdadsrid= sqlsrv1?.queryForObject<Int>("SELECT sdadsrid from z_pda where salesmanid="+salesmanid.toString())

        //val ftrid=sqlsrv1?.queryForObject<Int>("select max(id) from salestrades f where f.dsrid="+sdadsrid.toString()+" and f.trndate=DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0) and f.selid="+salesmanid)

        //val query="select st.iteid,sum(st.qty) from salestrades f,stockitemtrans st where st.slsid=f.id and f.id="+ftrid.toString()+" group by st.iteid"
        val query="select iteid,balanceqty from stockitemfindata"
        sqlsrv1?.query(query){
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO storefindata (iteid,startqty,qty) values("+resultSet.getInt(1).toString()+","
                        +resultSet.getFloat(2).toString()+","+resultSet.getFloat(2).toString()+")")

            }  while (resultSet.next())
            sqlite2?.update("END")

        }

        println("STOREFINDATA")

        /////STORECUSTDATA
        sqlite2?.update("DELETE from storecustdata")
        val query1="select a.iteid,a.cusid, CONVERT(varchar, a.lastdate, 101),a.lastprice,a.lastprcdisc,a.lastprcdisc2,(select top 1 qty from stockitemtrans b " +
                "inner join salestrades c on b.slsid=c.id where a.iteid=b.iteid and a.cusid=c.cusid and a.lastdate=b.trndate and b.outvalflag=1)from STOCKITEMCUSFINDATA a"

        sqlsrv1?.query(query1){
            resultSet->
            sqlite2?.update("BEGIN")
            do {
                sqlite2?.update("INSERT INTO storecustdata (iteid,cusid,lastqty,lastprice,lastdiscount,lastdiscount2,lastdate) values("+
                        resultSet.getInt(1).toString()+","+resultSet.getInt(2).toString()+
                        ","+resultSet.getFloat(7).toString()+","+resultSet.getFloat(4).toString()+
                        ","+resultSet.getFloat(5).toString()+","+resultSet.getFloat(6).toString()+
                        ",'"+resultSet.getString(3)+"')")

            }  while (resultSet.next())
            sqlite2?.update("END")

        }


        println("STORECUSTDATA")




        /////////DOCSERIES



        var codeId:Int=0
        var lastno:Int?=0

        codeId=sqlsrv1?.queryForObject<Int>("SELECT idx from salestradeseries d,z_pda z where d.idx=z.tdadsrid and z.salesmanid="+salesmanid.toString())!!


                //edo eimai


        sqlite2?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=1")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=1")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnum),0) from salestrades where year(trndate)=year(getdate()) and dsrid="+codeId.toString())

        var qry="UPDATE docseries set lastno="+lastno.toString()+" where id=1"

        sqlite2?.update(qry)




        codeId=sqlsrv1?.queryForObject<Int>("SELECT idx from salestradeseries d,z_pda z where d.idx=z.pisdsrid and z.salesmanid="+salesmanid.toString())!!



        sqlite2?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=2")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=2")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnum),0) from salestrades where year(trndate)=year(getdate()) and dsrid="+codeId.toString())

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=2")



        codeId=sqlsrv1?.queryForObject<Int>("SELECT idx from salestradeseries d,z_pda z where d.idx=z.dadsrid and z.salesmanid="+salesmanid.toString())!!



        sqlite2?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=3")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=3")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnum),0) from salestrades where year(trndate)=year(getdate()) and dsrid="+codeId.toString())

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=3")



        codeId=sqlsrv1?.queryForObject<Int>("SELECT idx from salestradeseries d,z_pda z where d.idx=z.depdsrid and z.salesmanid="+salesmanid.toString())!!



        sqlite1?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=4")
        // sqlite1?.update("UPDATE docseries set series='"+series+"'where id=4")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnum),0) from salestrades where year(trndate)=year(getdate()) and dsrid="+codeId.toString())

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=4")


        codeId=sqlsrv1?.queryForObject<Int>("SELECT idx from salestradeseries d,z_pda z where d.idx=z.pagdsrid and z.salesmanid="+salesmanid.toString())!!



        sqlite2?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=5")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=5")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnum),0) from salestrades where year(trndate)=year(getdate()) and dsrid="+codeId.toString())

        sqlite2?.update("UPDATE docseries set lastno="+lastno.toString()+" where id=5")




        codeId=sqlsrv1?.queryForObject<Int>("SELECT idx from salestradeseries d,z_pda z where d.idx=z.ddedsrid and z.salesmanid="+salesmanid.toString())!!


        //edo eimai


        sqlite2?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=6")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=1")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnum),0) from salestrades where year(trndate)=year(getdate()) and dsrid="+codeId.toString())

        qry="UPDATE docseries set lastno="+lastno.toString()+" where id=6"

        sqlite2?.update(qry)



        codeId=sqlsrv1?.queryForObject<Int>("SELECT idx from salestradeseries d,z_pda z where d.idx=z.sdadsrid and z.salesmanid="+salesmanid.toString())!!


        //edo eimai


        sqlite2?.update("UPDATE docseries set erpdsrid="+codeId.toString()+" where id=7")
        //sqlite1?.update("UPDATE docseries set series='"+series+"'where id=1")

        lastno=sqlsrv1?.queryForObject<Int>("SELECT isnull(max(dsrnum),0) from salestrades where year(trndate)=year(getdate()) and dsrid="+codeId.toString())

        qry="UPDATE docseries set lastno="+lastno.toString()+" where id=7"

        sqlite2?.update(qry)







        println("DOCSERIES")


        ///COMPANYDATA


        sqlite2?.update("DELETE from companydata")


        sqlsrv1?.query("select top 1 name,occupation,address,city,afm,doy,tel1,tel2,email,site from z_companydata"){
            resultSet->

            val query="INSERT INTO companydata (name,occupation,address,city,afm,doy,tel1,tel2,email,site) VALUES ('"+resultSet.getString(1)+"','"+resultSet.getString(2)+
                    "','"+resultSet.getString(3)+"','"+resultSet.getString(4)+"','"+resultSet.getString(5)+"','"+resultSet.getString(6)+"','"+
                    resultSet.getString(7)+"','"+resultSet.getString(8)+"','"+resultSet.getString(9)+"','"+resultSet.getString(10)+"')"
            sqlite2?.update("BEGIN")
            sqlite2?.update(query)
            sqlite2?.update("END")
        }



        println("COMPANYDATA")

        //sqlsrv1?.update("INSERT INTO z_log (task,backupfile,cdatetime) values (1,'algo.sqlite.db"+LocalDateTime.now().toString()+"-1',getdate())")
        println("Z_LOG")
        closeConnections()
    }


}