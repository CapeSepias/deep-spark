package com.stratio.deep.rdd;


import static scala.collection.JavaConversions.asScalaIterator;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.net.ssl.SSLException;
import org.apache.spark.InterruptibleIterator;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;
import scala.collection.Iterator;
import scala.reflect.ClassTag$;
import com.stratio.deep.config.IDeepJobConfig;
import com.stratio.deep.exception.DeepInstantiationException;
import com.stratio.deep.extractor.client.ExtractorClient;





/**
 * Created by rcrespo on 11/08/14.
 */
public class DeepRDD<T> extends RDD<T> implements Serializable {

  private static final long serialVersionUID = -5360986039609466526L;

//  private Broadcast<ExtractorClient<T>> extractorClient;

    private ExtractorClient<T> extractorClient;


    protected Broadcast<IDeepJobConfig<T, ? extends IDeepJobConfig<?, ?>>> config;

  public DeepRDD(SparkContext sc,
      IDeepJobConfig<T, IDeepJobConfig<T, ? extends IDeepJobConfig>> config) {
    super(sc, scala.collection.Seq$.MODULE$.empty(), ClassTag$.MODULE$.<T>apply(config
        .getEntityClass()));
    this.config =
        sc.broadcast(config, ClassTag$.MODULE$
            .<IDeepJobConfig<T, ? extends IDeepJobConfig<?, ?>>>apply(config.getClass()));

      try {
          ExtractorClient<T> extractor = new ExtractorClient<>();
          extractor.initialize();
//          this.extractorClient =sc.broadcast(extractor, ClassTag$.MODULE$.<ExtractorClient<T>>apply(extractor.getClass()));
      } catch (SSLException e) {
          e.printStackTrace();
      } catch (InterruptedException e) {
          e.printStackTrace();
      }
  }

  public DeepRDD(SparkContext sc, Class entityClass) {
    super(sc, scala.collection.Seq$.MODULE$.empty(), ClassTag$.MODULE$.<T>apply(entityClass));
  }

  @Override
  public Iterator<T> compute(Partition split, TaskContext context) {
      IDeepRecordReader IDeepRecordReader = initRecordReader(context, (IDeepPartition)split, config.value());
    if (extractorClient == null) {
      extractorClient = new ExtractorClient<>();
      try {
        extractorClient.initialize();
      } catch (SSLException | InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
//      new InterruptibleIterator<T>(ctx, asScalaIterator(recordReaderIterator));
      return new InterruptibleIterator<>(context , asScalaIterator(extractorClient.compute(IDeepRecordReader, config.getValue()))) ;


//    return new InterruptibleIterator<>(context , asScalaIterator(extractorClient.value().compute(IDeepRecordReader, config.getValue()))) ;
  }

  @Override
  public Partition[] getPartitions() {
    
    if (extractorClient == null) {
      extractorClient = new ExtractorClient<>();
      try {
        extractorClient.initialize();
      } catch (SSLException | InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

      return extractorClient.getPartitions(config.getValue(), id());
//    return extractorClient.value().getPartitions(config.getValue(), id());
  }

  public Broadcast<IDeepJobConfig<T, ? extends IDeepJobConfig<?, ?>>> getConfig() {
    return config;
  }

    /**
     * Instantiates a new deep record reader object associated to the provided partition.
     *
     * @param ctx the spark task context.
     * @param dp a spark deep partition
     * @return the deep record reader associated to the provided partition.
     */
    private IDeepRecordReader initRecordReader(TaskContext ctx, final IDeepPartition dp,
                                              IDeepJobConfig<T, ? extends IDeepJobConfig<?, ?>> config) {
        Class<?> recordReaderClass = config.getRecordReaderClass();
        Constructor c = recordReaderClass.getConstructors()[0];
//        new DeepRecordReader(config, dp.splitWrapper());
        try {
            IDeepRecordReader recordReader= (IDeepRecordReader)c.newInstance(config, dp.splitWrapper());
            ctx.addOnCompleteCallback(new OnComputedRDDCallback(recordReader));
            return recordReader;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
       ;
        return null;

    }
}