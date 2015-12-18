package fina2.metadata.jaxb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class Main {
	public static void main(String[] args) {
		try {
			JAXBContext jc = JAXBContext.newInstance("fina2.metadata.jaxb");
			Marshaller m = jc.createMarshaller();

			MDT mdt = new MDT();

			MDTNodeData node = new MDTNodeData();
			MDTNodeComparison mdtNodeCompOne = new MDTNodeComparison();
			MDTNodeComparison mdtNodeCompSec = new MDTNodeComparison();
			MDTComparison mdtComp = new MDTComparison();
			MDTDescription mdtDesc = new MDTDescription();
			MDTNodeDescription mdtEngNodeDesc = new MDTNodeDescription();
			MDTNodeDescription mdtFrNodeDesc = new MDTNodeDescription();
			MDTDependentNode mdtDepNode = new MDTDependentNode();

			List<Long> ids = new ArrayList<Long>();
			List<MDTNodeData> nodeList = mdt.getNODE();
			List<MDTNodeComparison> nodeCompList = new ArrayList<MDTNodeComparison>();
			
			List<MDTDescription> descriptions = new ArrayList<MDTDescription>();
			List<MDTNodeDescription> mdtDescriptions = new ArrayList<MDTNodeDescription>();
			

			node.setCODE("SOME CODE");
			node.setDATATYPE(1);
			node.setDISABLED(1);
			node.setEQUATION("SOME EQUATION");
			node.setEVALMETHOD(1);
			node.setID(1);
			node.setPARENTID(11);
			node.setREQUIRED(1);
			node.setSEQUENCE(1);
			node.setTYPE(0);

			mdtEngNodeDesc.setLANG_CODE("en_US");
			mdtEngNodeDesc.setVALUE("SOME ENGLISH STRING");
			mdtFrNodeDesc.setLANG_CODE("fr_FR");
			mdtFrNodeDesc.setVALUE("SOME FRENCH STRING");
			mdtDescriptions.add(mdtEngNodeDesc);
			mdtDescriptions.add(mdtFrNodeDesc);
			mdtDesc.setDESCRIPTION(mdtDescriptions);
			node.setDESCRIPTIONS(mdtDesc);

			mdtNodeCompOne.setCONDITION(1);
			mdtNodeCompOne.setEQUATION("COMARISON EQUATION");
			mdtNodeCompOne.setID(222);
			mdtNodeCompOne.setNODEID(1);
			
			mdtNodeCompSec.setCONDITION(1);
			mdtNodeCompSec.setEQUATION("COMPARISON EQUATION");
			mdtNodeCompSec.setID(333);
			mdtNodeCompSec.setNODEID(1);
			nodeCompList.add(mdtNodeCompSec);
			nodeCompList.add(mdtNodeCompOne);

			mdtComp.setCOMPARISON(nodeCompList);
			
			node.setCOMPARISONS(mdtComp);
			nodeList.add(node);

			ids.add(111l);
			ids.add(222l);
			mdtDepNode.setID(ids);
			node.setDEPENDENT_NODES(mdtDepNode);

			mdt.setNODE(nodeList);

			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(mdt, new File("exportedMdt.xml"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
