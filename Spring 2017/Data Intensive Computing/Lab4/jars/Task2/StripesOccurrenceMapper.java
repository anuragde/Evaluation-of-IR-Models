import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;

public class StripesOccurrenceMapper extends Mapper<LongWritable,Text,Text,MapWritable> {
    private MapWritable occurrenceMap = new MapWritable();
    private Text word = new Text();
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] tokens = value.toString().split("\\s+");
        int neighbors = context.getConfiguration().getInt("neighbors",tokens.length);
        if (tokens.length > 1) {
            for (int i = 0; i < tokens.length; i++) {
                word.set(tokens[i]);
                occurrenceMap.clear();

                int start = (i - neighbors < 0) ? 0 : i - neighbors;
                int end = (i + neighbors >= tokens.length) ? tokens.length - 1 : i + neighbors;
                for (int j = start; j <= end; j++) {
                    if (j == i) continue;
                    Text neighbor = new Text(tokens[j]);
                    if(occurrenceMap.containsKey(neighbor)){
                       IntWritable count = (IntWritable)occurrenceMap.get(neighbor);
                       count.set(count.get()+1);
                    }else{
                        occurrenceMap.put(neighbor,new IntWritable(1));
                    }
                }
              context.write(word,occurrenceMap);
            }
        }
    }
}
