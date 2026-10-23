package ast;

public interface ASTVisitor {
    public void visit(Program p);
    public void visit(BlockCommand c);
    public void visit(AssignCommand c);
    public void visit(PrintCommand c);
    public void visit(SumExpr e);
    public void visit(SubExpr e);
    public void visit(MulExpr e);
    public void visit(DivExpr e);
    public void visit(ModExpr e);
    public void visit(PowExpr e);
    public void visit(UMinusExpr e);
    public void visit(SinExpr e);
    public void visit(CosExpr e);
    public void visit(NumberExpr e);
    public void visit(StringExpr e);
    public void visit(PiExpr e);
    public void visit(IdExpr e);
}