package com.rapidminerchina.extension.dl4j.word2vec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeEnumeration;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;

public class Word2VecLearner extends Operator {

	/*
	 * just in case in some later version, there would be some input generated by other process to this operator
	 */
//	private final InputPort txt = getInputPorts().createPort("txt files");
	private final OutputPort model = getOutputPorts().createPort("model");
	private final OutputPort vector = getOutputPorts().createPort("vector");
	
	public static final String PARAMETER_USE_SPECIFIED_PATH = "use_specified_file_path";
	public static final String PARAMETER_FILE_PATH = "file_path";
	public static final String[] PARAMETER_ALLOWED_FILE_TYPE = new String[]{
		"txt"
		};
	public static final String PARAMETER_WORD_VECTOR_LENGTH = "word_vector_length";
	public static final String PARAMETER_ADA_GRAD_IN_TRAINING = "use_ada_grad_in_training";
	public static final String PARAMETER_LEARNING_RATE = "learning_rate";
	public static final String PARAMETER_MIN_WORD_FREQUENCY = "min_word_frequency";
	public static final String PARAMETER_ITERATION = "iteration";
	public static final String PARAMETER_HIDDEN_LAYER_SIZE = "hidden_layer_size";
	public static final String PARAMETER_STOP_WORDS = "stop_words";
	public static final String PARAMETER_WINDOW_SIZE = "window_size";
	public static final String PARAMETER_USE_LOCAL_RANDOM_SEED = "use_local_random_seed";
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	public Word2VecLearner(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		ParameterType type = null;
		
//		types.add(new ParameterTypeBoolean(
//				PARAMETER_USE_SPECIFIED_PATH,
//				"Indicates if to use specified file path for the input.",
//				true));
		
		type = new ParameterTypeFile(
				PARAMETER_FILE_PATH,
				"The path of the raw sentneces.",
				true,
				PARAMETER_ALLOWED_FILE_TYPE);
		
		type.setExpert(false);
//		type.registerDependencyCondition(
//				new BooleanParameterCondition(
//						this, 
//						PARAMETER_USE_SPECIFIED_PATH, 
//						false,true));
		types.add(type);
		
		types.add(new ParameterTypeInt(
				PARAMETER_WORD_VECTOR_LENGTH,
				"the length of vector converted by each word",
				1,Integer.MAX_VALUE,100
				));
		
		types.add(new ParameterTypeBoolean(
				PARAMETER_ADA_GRAD_IN_TRAINING,
				"Indicates if to use ADA grad in training the vectors.",
				false));
		
		types.add(new ParameterTypeDouble(
				PARAMETER_LEARNING_RATE,
				"The learning rate.",
				Double.MIN_VALUE, 1.0d, 0.02));
		
		types.add(new ParameterTypeInt(
				PARAMETER_MIN_WORD_FREQUENCY,
				"The minimum time of occurance to register a word in the table of vector",
				1,Integer.MAX_VALUE,5
				));
		
		types.add(new ParameterTypeInt(
				PARAMETER_ITERATION,
				"The number of training iterations on the raw sentances.",
				1,Integer.MAX_VALUE,1
				));
		
		types.add(new ParameterTypeInt(
				PARAMETER_HIDDEN_LAYER_SIZE,
				"The number of nodes in the hidden layer.",
				1,Integer.MAX_VALUE,100
				));
		
		type = new ParameterTypeEnumeration(
				PARAMETER_STOP_WORDS,
				"The stop words",
				new ParameterTypeString("One stop word", "a stop word that is ignored in the training.",""
				));
				
		types.add(type);
		
		types.add(new ParameterTypeInt(
				PARAMETER_WINDOW_SIZE,
				"The window size for skip-gram. The n in n-grams",
				1,Integer.MAX_VALUE,5
				));
		
		type = new ParameterTypeBoolean(
				PARAMETER_USE_LOCAL_RANDOM_SEED,
				"Indicates if to set the value of random seed.",
				false);
		type.setExpert(true);
		types.add(type);
		
		type = new ParameterTypeInt(
				PARAMETER_LOCAL_RANDOM_SEED,
				"The value of random seed",
				1, Integer.MAX_VALUE, 1992);
		
		type.setExpert(true);
		type.registerDependencyCondition(
				new BooleanParameterCondition(
						this, 
						PARAMETER_USE_LOCAL_RANDOM_SEED, 
						false,true));
		types.add(type);
		
		return types;
	}
	
	
	@Override
	public void doWork() throws OperatorException{
		
//		boolean specifyPath = getParameterAsBoolean(PARAMETER_USE_SPECIFIED_PATH);
		
		String path = "";
		
//		if (specifyPath) {
			path = getParameterAsString(PARAMETER_FILE_PATH);
			
			if (path == null){
				throw new OperatorException("Please specify a file using the file explorer,"
						+ "or input raw sentences from the txt port.");
			}
			
//		} else {
			// use file input port
			/* *************************************************************************************
			 * Not necessary in current implementation
			 * Don't Forget to throw exceptions
			 * *************************************************************************************
			 */
//		}
		
		int vectorLength = getParameterAsInt(PARAMETER_WORD_VECTOR_LENGTH);
		boolean adaGrad = getParameterAsBoolean(PARAMETER_ADA_GRAD_IN_TRAINING);
		double lr = getParameterAsDouble(PARAMETER_LEARNING_RATE);
		int minWordFrequency = getParameterAsInt(PARAMETER_MIN_WORD_FREQUENCY);
		int numIteration = getParameterAsInt(PARAMETER_ITERATION);
		int hiddenLayer = getParameterAsInt(PARAMETER_HIDDEN_LAYER_SIZE);
		
		String[] stopWords = ParameterTypeEnumeration.transformString2Enumeration(
				getParameterAsString(PARAMETER_STOP_WORDS));
		ArrayList<String> stopWordsList = new ArrayList<String>();
		for(int i =0; i< stopWords.length; i++){
			stopWordsList.add(stopWords[i]);
		}
		
		int windowSize = getParameterAsInt(PARAMETER_WINDOW_SIZE);
		
		long seed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED);
		
		SentenceIterator iterator  = null;
		// Configuration phase
		try {
			
			iterator = new BasicLineIterator(path);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    TokenizerFactory token = new DefaultTokenizerFactory();
	    token.setTokenPreProcessor(new CommonPreprocessor());
	    InMemoryLookupCache cache = new InMemoryLookupCache();
	    WeightLookupTable<VocabWord> table = new InMemoryLookupTable.Builder<VocabWord>()
                .vectorLength(vectorLength)
                .useAdaGrad(adaGrad)
                .cache(cache)
                .lr(lr)
                .build();
	    
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(minWordFrequency)
                .iterations(numIteration)
                .layerSize(hiddenLayer)
                .lookupTable(table)
                .epochs(1)
                .stopWords(stopWordsList)
                .vocabCache(cache)
                .seed(seed)
                .windowSize(windowSize)
                .iterate(iterator)
                .tokenizerFactory(token)
                .build();
        
        vec.fit();
        
        // construct the matrix for the example set
        int numRows = vec.vocab().numWords();
        int numColumns = vectorLength + 1;
        
        Object[][] data = new Object[numRows][numColumns];
        int rowIndex = 0;
        
        // for each word in the vocabulary, combine the word with its numerical vector in one row
        Collection<String> words = vec.vocab().words();
        for (String word : words){
        	data[rowIndex][0] = word;
        	double[] wordVector = vec.getWordVector(word);
        	for(int j=1; j<numColumns; j++){
            	data[rowIndex][j] = wordVector[j-1];
        	}
        	rowIndex ++;
        }
        
        ExampleSet resultVector = ExampleSetFactory.createExampleSet(data);
        
        resultVector.getAttributes().get("att1").setName("Word");
        
        for (int i=2; i<=numColumns; i++){
        	String oldName = "att" + i;
        	String newName = "att" + (i-1);
        	resultVector.getAttributes().get(oldName).setName(newName);
        }
        
        vector.deliver(resultVector);
        
        Word2VecModel resultModel = new Word2VecModel(ExampleSetFactory.createExampleSet(new double [1][1]));
        resultModel.setModel(vec);
        resultModel.setResultTable(resultVector);
        
        model.deliver(resultModel);
	}
	
}
